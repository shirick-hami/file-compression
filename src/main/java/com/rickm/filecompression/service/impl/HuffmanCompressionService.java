package com.rickm.filecompression.service.impl;

import com.rickm.filecompression.model.CompressionResult;
import com.rickm.filecompression.model.ProgressInfo;
import com.rickm.filecompression.service.CompressionService;
import com.rickm.filecompression.utils.HuffmanCodec;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service layer for compression and decompression operations.
 * Provides progress tracking and statistics.
 */
@Service
public class HuffmanCompressionService implements CompressionService {
    
    private final Map<String, AtomicReference<ProgressInfo>> progressMap = new ConcurrentHashMap<>();
    private final Map<String, CompressionResult> resultMap = new ConcurrentHashMap<>();
    
    /**
     * Compresses the given data using Huffman encoding.
     * 
     * @param data The data to compress
     * @param fileName The original file name
     * @return CompressionResult with compressed data and statistics
     */
    @Override
    public CompressionResult compress(byte[] data, String fileName) {
        String operationId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        
        // Initialize progress tracking
        AtomicReference<ProgressInfo> progressRef = new AtomicReference<>(
            ProgressInfo.builder()
                .operationId(operationId)
                .fileName(fileName)
                .status(ProgressInfo.OperationStatus.QUEUED)
                .totalBytes(data.length)
                .startTime(startTime)
                .build()
        );
        progressMap.put(operationId, progressRef);
        
        try {
            // Create codec with progress callback
            HuffmanCodec codec = new HuffmanCodec(builder -> {
                ProgressInfo current = progressRef.get();
                progressRef.set(ProgressInfo.builder()
                    .operationId(operationId)
                    .fileName(fileName)
                    .status(builder.build().getStatus())
                    .progressPercent(builder.build().getProgressPercent())
                    .currentPhase(builder.build().getCurrentPhase())
                    .totalBytes(data.length)
                    .startTime(startTime)
                    .lastUpdated(Instant.now())
                    .build());
            });
            
            // Perform compression
            byte[] compressedData = codec.compress(data);
            
            // Update progress to completed
            progressRef.set(ProgressInfo.builder()
                .operationId(operationId)
                .fileName(fileName)
                .status(ProgressInfo.OperationStatus.COMPLETED)
                .progressPercent(100)
                .currentPhase("Completed")
                .bytesProcessed(data.length)
                .totalBytes(data.length)
                .startTime(startTime)
                .lastUpdated(Instant.now())
                .build());
            
            // Calculate compression ratio
            double compressionRatio = data.length > 0 
                ? 1.0 - ((double) compressedData.length / data.length) 
                : 0;
            
            long processingTime = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            
            CompressionResult result = CompressionResult.builder()
                .operationId(operationId)
                .fileName(fileName + ".huff")
                .originalSize(data.length)
                .processedSize(compressedData.length)
                .compressionRatio(compressionRatio)
                .processingTimeMs(processingTime)
                .operationType(CompressionResult.OperationType.COMPRESS)
                .success(true)
                .data(compressedData)
                .build();
            
            resultMap.put(operationId, result);
            return result;
            
        } catch (IOException e) {
            progressRef.set(ProgressInfo.builder()
                .operationId(operationId)
                .fileName(fileName)
                .status(ProgressInfo.OperationStatus.FAILED)
                .currentPhase("Failed")
                .errorMessage(e.getMessage())
                .startTime(startTime)
                .lastUpdated(Instant.now())
                .build());
            
            return CompressionResult.builder()
                .operationId(operationId)
                .fileName(fileName)
                .originalSize(data.length)
                .operationType(CompressionResult.OperationType.COMPRESS)
                .success(false)
                .errorMessage("Compression failed: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Decompresses Huffman encoded data.
     * 
     * @param compressedData The compressed data
     * @param fileName The compressed file name
     * @return CompressionResult with decompressed data
     */
    @Override
    public CompressionResult decompress(byte[] compressedData, String fileName) {
        String operationId = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        
        // Validate file format first
        HuffmanCodec codec = new HuffmanCodec();
        if (!codec.isValidCompressedFile(compressedData)) {
            return CompressionResult.builder()
                .operationId(operationId)
                .fileName(fileName)
                .originalSize(compressedData.length)
                .operationType(CompressionResult.OperationType.DECOMPRESS)
                .success(false)
                .errorMessage("Invalid file format: not a Huffman compressed file (.huff)")
                .build();
        }
        
        // Initialize progress tracking
        AtomicReference<ProgressInfo> progressRef = new AtomicReference<>(
            ProgressInfo.builder()
                .operationId(operationId)
                .fileName(fileName)
                .status(ProgressInfo.OperationStatus.QUEUED)
                .totalBytes(compressedData.length)
                .startTime(startTime)
                .build()
        );
        progressMap.put(operationId, progressRef);
        
        try {
            // Create codec with progress callback
            HuffmanCodec codecWithProgress = new HuffmanCodec(builder -> {
                progressRef.set(ProgressInfo.builder()
                    .operationId(operationId)
                    .fileName(fileName)
                    .status(builder.build().getStatus())
                    .progressPercent(builder.build().getProgressPercent())
                    .currentPhase(builder.build().getCurrentPhase())
                    .totalBytes(compressedData.length)
                    .startTime(startTime)
                    .lastUpdated(Instant.now())
                    .build());
            });
            
            // Perform decompression
            byte[] decompressedData = codecWithProgress.decompress(compressedData);
            
            // Update progress to completed
            progressRef.set(ProgressInfo.builder()
                .operationId(operationId)
                .fileName(fileName)
                .status(ProgressInfo.OperationStatus.COMPLETED)
                .progressPercent(100)
                .currentPhase("Completed")
                .bytesProcessed(decompressedData.length)
                .totalBytes(compressedData.length)
                .startTime(startTime)
                .lastUpdated(Instant.now())
                .build());
            
            // Calculate expansion ratio (inverse of compression)
            double expansionRatio = compressedData.length > 0 
                ? ((double) decompressedData.length / compressedData.length) - 1.0 
                : 0;
            
            long processingTime = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            
            // Remove .huff extension if present
            String outputFileName = fileName;
            if (outputFileName.toLowerCase().endsWith(".huff")) {
                outputFileName = outputFileName.substring(0, outputFileName.length() - 5);
            } else {
                outputFileName = "decompressed_" + outputFileName;
            }
            
            CompressionResult result = CompressionResult.builder()
                .operationId(operationId)
                .fileName(outputFileName)
                .originalSize(compressedData.length)
                .processedSize(decompressedData.length)
                .compressionRatio(expansionRatio)
                .processingTimeMs(processingTime)
                .operationType(CompressionResult.OperationType.DECOMPRESS)
                .success(true)
                .data(decompressedData)
                .build();
            
            resultMap.put(operationId, result);
            return result;
            
        } catch (IOException e) {
            progressRef.set(ProgressInfo.builder()
                .operationId(operationId)
                .fileName(fileName)
                .status(ProgressInfo.OperationStatus.FAILED)
                .currentPhase("Failed")
                .errorMessage(e.getMessage())
                .startTime(startTime)
                .lastUpdated(Instant.now())
                .build());
            
            return CompressionResult.builder()
                .operationId(operationId)
                .fileName(fileName)
                .originalSize(compressedData.length)
                .operationType(CompressionResult.OperationType.DECOMPRESS)
                .success(false)
                .errorMessage("Decompression failed: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Gets the current progress for an operation.
     */
    @Override
    public ProgressInfo getProgress(String operationId) {
        AtomicReference<ProgressInfo> ref = progressMap.get(operationId);
        return ref != null ? ref.get() : null;
    }
    
    /**
     * Gets the result for a completed operation.
     */
    @Override
    public CompressionResult getResult(String operationId) {
        return resultMap.get(operationId);
    }
    
    /**
     * Analyzes data and returns compression statistics without actually compressing.
     */
    @Override
    public Map<String, Object> analyzeData(byte[] data) {
        HuffmanCodec codec = new HuffmanCodec();
        return codec.getCodeStatistics(data);
    }
    
    /**
     * Checks if a file can be decompressed by this service.
     */
    @Override
    public boolean canDecompress(byte[] data) {
        HuffmanCodec codec = new HuffmanCodec();
        return codec.isValidCompressedFile(data);
    }
    
    /**
     * Clears old progress and result entries.
     */
    @Override
    public void cleanup(String operationId) {
        progressMap.remove(operationId);
        resultMap.remove(operationId);
    }
}
