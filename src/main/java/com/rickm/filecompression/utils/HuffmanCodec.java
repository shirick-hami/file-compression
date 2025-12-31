package com.rickm.filecompression.utils;

import com.rickm.filecompression.model.HuffmanNode;
import com.rickm.filecompression.model.ProgressInfo;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * Core Huffman encoding/decoding implementation.
 * This class provides the algorithms for building Huffman trees,
 * generating codes, and performing compression/decompression.
 */
public class HuffmanCodec {
    
    // Magic number to identify Huffman compressed files
    public static final byte[] MAGIC_NUMBER = {0x48, 0x55, 0x46, 0x46}; // "HUFF"
    public static final int VERSION = 1;
    
    private final Consumer<ProgressInfo.Builder> progressCallback;
    
    public HuffmanCodec() {
        this(null);
    }
    
    public HuffmanCodec(Consumer<ProgressInfo.Builder> progressCallback) {
        this.progressCallback = progressCallback;
    }
    
    /**
     * Compresses the input byte array using Huffman coding.
     * 
     * @param data The data to compress
     * @return The compressed data with header information
     * @throws IOException If compression fails
     */
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return createEmptyCompressedFile();
        }
        
        updateProgress(ProgressInfo.OperationStatus.BUILDING_FREQUENCY_TABLE, 
                      "Building frequency table", 5);
        
        // Step 1: Build frequency table
        int[] frequencies = buildFrequencyTable(data);
        
        updateProgress(ProgressInfo.OperationStatus.BUILDING_HUFFMAN_TREE, 
                      "Building Huffman tree", 15);
        
        // Step 2: Build Huffman tree
        HuffmanNode root = buildHuffmanTree(frequencies);
        
        if (root == null) {
            return createEmptyCompressedFile();
        }
        
        updateProgress(ProgressInfo.OperationStatus.GENERATING_CODES, 
                      "Generating Huffman codes", 25);
        
        // Step 3: Generate codes
        Map<Byte, String> codes = new HashMap<>();
        generateCodes(root, "", codes);
        
        updateProgress(ProgressInfo.OperationStatus.ENCODING, 
                      "Encoding data", 35);
        
        // Step 4: Encode data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(outputStream);
        
        // Write header
        writeHeader(dataOut, data.length, frequencies);
        
        // Encode the data
        BitOutputStream bitOut = new BitOutputStream(outputStream);
        
        int processedBytes = 0;
        int progressUpdateInterval = Math.max(1, data.length / 100);
        
        for (byte b : data) {
            String code = codes.get(b);
            for (char bit : code.toCharArray()) {
                bitOut.writeBit(bit == '1');
            }
            
            processedBytes++;
            if (processedBytes % progressUpdateInterval == 0) {
                int progress = 35 + (int)((processedBytes * 60.0) / data.length);
                updateProgress(ProgressInfo.OperationStatus.ENCODING, 
                              "Encoding data", progress);
            }
        }
        
        bitOut.flush();
        
        updateProgress(ProgressInfo.OperationStatus.WRITING_OUTPUT, 
                      "Finalizing output", 95);
        
        return outputStream.toByteArray();
    }
    
    /**
     * Decompresses Huffman encoded data.
     * 
     * @param compressedData The compressed data to decompress
     * @return The original uncompressed data
     * @throws IOException If decompression fails or data is invalid
     */
    public byte[] decompress(byte[] compressedData) throws IOException {
        if (compressedData == null || compressedData.length < 8) {
            throw new IOException("Invalid compressed data: too short");
        }
        
        updateProgress(ProgressInfo.OperationStatus.READING_FILE, 
                      "Reading compressed data", 5);
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        DataInputStream dataIn = new DataInputStream(inputStream);
        
        // Verify magic number
        byte[] magic = new byte[4];
        dataIn.readFully(magic);
        if (!Arrays.equals(magic, MAGIC_NUMBER)) {
            throw new IOException("Invalid file format: not a Huffman compressed file");
        }
        
        // Read version
        int version = dataIn.readInt();
        if (version != VERSION) {
            throw new IOException("Unsupported file version: " + version);
        }
        
        // Read original size
        int originalSize = dataIn.readInt();
        
        if (originalSize == 0) {
            return new byte[0];
        }
        
        updateProgress(ProgressInfo.OperationStatus.BUILDING_FREQUENCY_TABLE, 
                      "Reading frequency table", 15);
        
        // Read frequency table
        int[] frequencies = readFrequencyTable(dataIn);
        
        updateProgress(ProgressInfo.OperationStatus.BUILDING_HUFFMAN_TREE, 
                      "Rebuilding Huffman tree", 25);
        
        // Rebuild Huffman tree
        HuffmanNode root = buildHuffmanTree(frequencies);
        
        if (root == null) {
            throw new IOException("Failed to rebuild Huffman tree");
        }
        
        updateProgress(ProgressInfo.OperationStatus.DECODING, 
                      "Decoding data", 35);
        
        // Decode the data
        byte[] result = new byte[originalSize];
        BitInputStream bitIn = new BitInputStream(inputStream);
        
        HuffmanNode current = root;
        int decodedBytes = 0;
        int progressUpdateInterval = Math.max(1, originalSize / 100);
        
        while (decodedBytes < originalSize) {
            // Handle single-symbol case
            if (root.isLeaf()) {
                result[decodedBytes++] = root.getData();
            } else {
                boolean bit = bitIn.readBit();
                current = bit ? current.getRight() : current.getLeft();
                
                if (current.isLeaf()) {
                    result[decodedBytes++] = current.getData();
                    current = root;
                }
            }
            
            if (decodedBytes % progressUpdateInterval == 0) {
                int progress = 35 + (int)((decodedBytes * 60.0) / originalSize);
                updateProgress(ProgressInfo.OperationStatus.DECODING, 
                              "Decoding data", progress);
            }
        }
        
        updateProgress(ProgressInfo.OperationStatus.WRITING_OUTPUT, 
                      "Finalizing output", 95);
        
        return result;
    }
    
    /**
     * Checks if the given data is a valid Huffman compressed file.
     */
    public boolean isValidCompressedFile(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        return data[0] == MAGIC_NUMBER[0] && 
               data[1] == MAGIC_NUMBER[1] && 
               data[2] == MAGIC_NUMBER[2] && 
               data[3] == MAGIC_NUMBER[3];
    }
    
    /**
     * Builds a frequency table for all bytes in the input data.
     */
    public int[] buildFrequencyTable(byte[] data) {
        int[] frequencies = new int[256];
        for (byte b : data) {
            frequencies[b & 0xFF]++;
        }
        return frequencies;
    }
    
    /**
     * Builds a Huffman tree from the frequency table.
     */
    public HuffmanNode buildHuffmanTree(int[] frequencies) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        
        // Create leaf nodes for each byte with non-zero frequency
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                pq.offer(new HuffmanNode((byte) i, frequencies[i]));
            }
        }
        
        if (pq.isEmpty()) {
            return null;
        }
        
        // Handle single symbol case
        if (pq.size() == 1) {
            HuffmanNode single = pq.poll();
            return new HuffmanNode(single, new HuffmanNode((byte) 0, 0));
        }
        
        // Build the tree by repeatedly combining the two lowest frequency nodes
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            pq.offer(new HuffmanNode(left, right));
        }
        
        return pq.poll();
    }
    
    /**
     * Generates Huffman codes for each byte by traversing the tree.
     */
    public void generateCodes(HuffmanNode node, String code, Map<Byte, String> codes) {
        if (node == null) {
            return;
        }
        
        if (node.isLeaf()) {
            codes.put(node.getData(), code.isEmpty() ? "0" : code);
            return;
        }
        
        generateCodes(node.getLeft(), code + "0", codes);
        generateCodes(node.getRight(), code + "1", codes);
    }
    
    /**
     * Gets statistics about the Huffman codes.
     */
    public Map<String, Object> getCodeStatistics(byte[] data) {
        Map<String, Object> stats = new HashMap<>();
        
        int[] frequencies = buildFrequencyTable(data);
        HuffmanNode root = buildHuffmanTree(frequencies);
        
        Map<Byte, String> codes = new HashMap<>();
        if (root != null) {
            generateCodes(root, "", codes);
        }
        
        // Calculate average code length
        double totalBits = 0;
        int totalSymbols = 0;
        int maxCodeLength = 0;
        int minCodeLength = Integer.MAX_VALUE;
        
        for (Map.Entry<Byte, String> entry : codes.entrySet()) {
            int freq = frequencies[entry.getKey() & 0xFF];
            int codeLength = entry.getValue().length();
            totalBits += freq * codeLength;
            totalSymbols += freq;
            maxCodeLength = Math.max(maxCodeLength, codeLength);
            minCodeLength = Math.min(minCodeLength, codeLength);
        }
        
        stats.put("uniqueSymbols", codes.size());
        stats.put("totalSymbols", totalSymbols);
        stats.put("averageCodeLength", totalSymbols > 0 ? totalBits / totalSymbols : 0);
        stats.put("maxCodeLength", maxCodeLength);
        stats.put("minCodeLength", minCodeLength == Integer.MAX_VALUE ? 0 : minCodeLength);
        stats.put("theoreticalCompression", totalSymbols > 0 ? (totalBits / 8.0) / totalSymbols : 1);
        
        return stats;
    }
    
    private byte[] createEmptyCompressedFile() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(outputStream);
        
        dataOut.write(MAGIC_NUMBER);
        dataOut.writeInt(VERSION);
        dataOut.writeInt(0); // Original size = 0
        dataOut.writeShort(0); // No symbols
        
        return outputStream.toByteArray();
    }
    
    private void writeHeader(DataOutputStream dataOut, int originalSize, int[] frequencies) 
            throws IOException {
        // Write magic number
        dataOut.write(MAGIC_NUMBER);
        
        // Write version
        dataOut.writeInt(VERSION);
        
        // Write original size
        dataOut.writeInt(originalSize);
        
        // Count non-zero frequencies
        int symbolCount = 0;
        for (int freq : frequencies) {
            if (freq > 0) symbolCount++;
        }
        
        // Write symbol count
        dataOut.writeShort(symbolCount);
        
        // Write frequency table (only non-zero entries)
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                dataOut.writeByte(i);
                dataOut.writeInt(frequencies[i]);
            }
        }
    }
    
    private int[] readFrequencyTable(DataInputStream dataIn) throws IOException {
        int[] frequencies = new int[256];
        
        int symbolCount = dataIn.readShort() & 0xFFFF;
        
        for (int i = 0; i < symbolCount; i++) {
            int symbol = dataIn.readByte() & 0xFF;
            int frequency = dataIn.readInt();
            frequencies[symbol] = frequency;
        }
        
        return frequencies;
    }
    
    private void updateProgress(ProgressInfo.OperationStatus status, String phase, int percent) {
        if (progressCallback != null) {
            progressCallback.accept(ProgressInfo.builder()
                    .status(status)
                    .currentPhase(phase)
                    .progressPercent(percent));
        }
    }
    
    /**
     * Helper class for writing bits to an output stream.
     */
    private static class BitOutputStream {
        private final OutputStream out;
        private int currentByte;
        private int numBitsFilled;
        
        public BitOutputStream(OutputStream out) {
            this.out = out;
            this.currentByte = 0;
            this.numBitsFilled = 0;
        }
        
        public void writeBit(boolean bit) throws IOException {
            currentByte = (currentByte << 1) | (bit ? 1 : 0);
            numBitsFilled++;
            
            if (numBitsFilled == 8) {
                out.write(currentByte);
                currentByte = 0;
                numBitsFilled = 0;
            }
        }
        
        public void flush() throws IOException {
            if (numBitsFilled > 0) {
                currentByte <<= (8 - numBitsFilled);
                out.write(currentByte);
            }
        }
    }
    
    /**
     * Helper class for reading bits from an input stream.
     */
    private static class BitInputStream {
        private final InputStream in;
        private int currentByte;
        private int numBitsRemaining;
        
        public BitInputStream(InputStream in) {
            this.in = in;
            this.currentByte = 0;
            this.numBitsRemaining = 0;
        }
        
        public boolean readBit() throws IOException {
            if (numBitsRemaining == 0) {
                currentByte = in.read();
                if (currentByte == -1) {
                    throw new IOException("Unexpected end of stream");
                }
                numBitsRemaining = 8;
            }
            
            numBitsRemaining--;
            return ((currentByte >> numBitsRemaining) & 1) == 1;
        }
    }
}
