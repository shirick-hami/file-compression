package com.rickm.filecompression.utils;

import com.rickm.filecompression.model.HuffmanNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive test suite for HuffmanCodec.
 */
@DisplayName("HuffmanCodec Tests")
class HuffmanCodecTest {

    private HuffmanCodec codec;

    @BeforeEach
    void setUp() {
        codec = new HuffmanCodec();
    }

    @Nested
    @DisplayName("Frequency Table Tests")
    class FrequencyTableTests {

        @Test
        @DisplayName("Should build correct frequency table for simple text")
        void buildFrequencyTable_simpleText() {
            byte[] data = "AAAAABBBCC".getBytes(StandardCharsets.UTF_8);
            int[] frequencies = codec.buildFrequencyTable(data);

            assertThat(frequencies['A']).isEqualTo(5);
            assertThat(frequencies['B']).isEqualTo(3);
            assertThat(frequencies['C']).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle empty input")
        void buildFrequencyTable_emptyInput() {
            byte[] data = new byte[0];
            int[] frequencies = codec.buildFrequencyTable(data);

            assertThat(frequencies).hasSize(256);
            assertThat(Arrays.stream(frequencies).sum()).isZero();
        }

        @Test
        @DisplayName("Should handle single byte")
        void buildFrequencyTable_singleByte() {
            byte[] data = {65}; // 'A'
            int[] frequencies = codec.buildFrequencyTable(data);

            assertThat(frequencies['A']).isEqualTo(1);
            assertThat(Arrays.stream(frequencies).sum()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle all byte values")
        void buildFrequencyTable_allByteValues() {
            byte[] data = new byte[256];
            for (int i = 0; i < 256; i++) {
                data[i] = (byte) i;
            }
            int[] frequencies = codec.buildFrequencyTable(data);

            for (int freq : frequencies) {
                assertThat(freq).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Should handle repeated bytes correctly")
        void buildFrequencyTable_repeatedBytes() {
            byte[] data = new byte[1000];
            Arrays.fill(data, (byte) 'X');
            int[] frequencies = codec.buildFrequencyTable(data);

            assertThat(frequencies['X']).isEqualTo(1000);
            assertThat(Arrays.stream(frequencies).sum()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("Huffman Tree Tests")
    class HuffmanTreeTests {

        @Test
        @DisplayName("Should build tree for simple frequency table")
        void buildHuffmanTree_simple() {
            int[] frequencies = new int[256];
            frequencies['A'] = 5;
            frequencies['B'] = 3;
            frequencies['C'] = 2;

            HuffmanNode root = codec.buildHuffmanTree(frequencies);

            assertThat(root).isNotNull();
            assertThat(root.getFrequency()).isEqualTo(10);
            assertThat(root.isLeaf()).isFalse();
        }

        @Test
        @DisplayName("Should return null for empty frequency table")
        void buildHuffmanTree_empty() {
            int[] frequencies = new int[256];

            HuffmanNode root = codec.buildHuffmanTree(frequencies);

            assertThat(root).isNull();
        }

        @Test
        @DisplayName("Should handle single symbol")
        void buildHuffmanTree_singleSymbol() {
            int[] frequencies = new int[256];
            frequencies['A'] = 5;

            HuffmanNode root = codec.buildHuffmanTree(frequencies);

            assertThat(root).isNotNull();
            assertThat(root.getFrequency()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should create valid tree structure")
        void buildHuffmanTree_validStructure() {
            int[] frequencies = new int[256];
            frequencies['A'] = 10;
            frequencies['B'] = 5;
            frequencies['C'] = 3;
            frequencies['D'] = 2;

            HuffmanNode root = codec.buildHuffmanTree(frequencies);

            // Total frequency should equal sum of all frequencies
            assertThat(root.getFrequency()).isEqualTo(20);
            
            // Tree should have proper structure
            assertThat(countLeaves(root)).isEqualTo(4);
        }

        private int countLeaves(HuffmanNode node) {
            if (node == null) return 0;
            if (node.isLeaf()) return 1;
            return countLeaves(node.getLeft()) + countLeaves(node.getRight());
        }
    }

    @Nested
    @DisplayName("Code Generation Tests")
    class CodeGenerationTests {

        @Test
        @DisplayName("Should generate codes for all symbols")
        void generateCodes_allSymbols() {
            int[] frequencies = new int[256];
            frequencies['A'] = 5;
            frequencies['B'] = 3;
            frequencies['C'] = 2;

            HuffmanNode root = codec.buildHuffmanTree(frequencies);
            Map<Byte, String> codes = new HashMap<>();
            codec.generateCodes(root, "", codes);

            assertThat(codes).hasSize(3);
            assertThat(codes.get((byte) 'A')).isNotNull().matches("[01]+");
            assertThat(codes.get((byte) 'B')).isNotNull().matches("[01]+");
            assertThat(codes.get((byte) 'C')).isNotNull().matches("[01]+");
        }

        @Test
        @DisplayName("Should generate prefix-free codes")
        void generateCodes_prefixFree() {
            int[] frequencies = new int[256];
            frequencies['A'] = 10;
            frequencies['B'] = 5;
            frequencies['C'] = 3;
            frequencies['D'] = 2;
            frequencies['E'] = 1;

            HuffmanNode root = codec.buildHuffmanTree(frequencies);
            Map<Byte, String> codes = new HashMap<>();
            codec.generateCodes(root, "", codes);

            // No code should be a prefix of another
            for (Map.Entry<Byte, String> entry1 : codes.entrySet()) {
                for (Map.Entry<Byte, String> entry2 : codes.entrySet()) {
                    if (!entry1.getKey().equals(entry2.getKey())) {
                        assertThat(entry1.getValue().startsWith(entry2.getValue())).isFalse();
                    }
                }
            }
        }

        @Test
        @DisplayName("More frequent symbols should have shorter or equal codes")
        void generateCodes_frequencyOrdering() {
            int[] frequencies = new int[256];
            frequencies['A'] = 100;  // Most frequent
            frequencies['B'] = 50;
            frequencies['C'] = 10;   // Least frequent

            HuffmanNode root = codec.buildHuffmanTree(frequencies);
            Map<Byte, String> codes = new HashMap<>();
            codec.generateCodes(root, "", codes);

            // Most frequent symbol should have shortest or equal code length
            assertThat(codes.get((byte) 'A').length())
                    .isLessThanOrEqualTo(codes.get((byte) 'C').length());
        }
    }

    @Nested
    @DisplayName("Compression Tests")
    class CompressionTests {

        @Test
        @DisplayName("Should compress and decompress to original")
        void compressDecompress_roundTrip() throws IOException {
            String original = "Hello, World! This is a test of Huffman compression.";
            byte[] data = original.getBytes(StandardCharsets.UTF_8);

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
            assertThat(new String(decompressed, StandardCharsets.UTF_8)).isEqualTo(original);
        }

        @Test
        @DisplayName("Should handle empty input")
        void compress_emptyInput() throws IOException {
            byte[] data = new byte[0];

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEmpty();
        }

        @Test
        @DisplayName("Should handle single byte")
        void compress_singleByte() throws IOException {
            byte[] data = {65};

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
        }

        @Test
        @DisplayName("Should handle repeated character")
        void compress_repeatedCharacter() throws IOException {
            byte[] data = new byte[100];
            Arrays.fill(data, (byte) 'A');

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 100, 1000, 10000})
        @DisplayName("Should handle various file sizes")
        void compress_variousSizes(int size) throws IOException {
            byte[] data = generateRandomText(size);

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
        }

        @Test
        @DisplayName("Should achieve compression on repetitive data")
        void compress_achievesCompression() throws IOException {
            // Create highly repetitive data
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("AAAA");
            }
            byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

            byte[] compressed = codec.compress(data);

            // Should achieve significant compression
            assertThat(compressed.length).isLessThan(data.length);
        }

        @Test
        @DisplayName("Should include magic number in compressed output")
        void compress_includesMagicNumber() throws IOException {
            byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

            byte[] compressed = codec.compress(data);

            assertThat(compressed[0]).isEqualTo((byte) 'H');
            assertThat(compressed[1]).isEqualTo((byte) 'U');
            assertThat(compressed[2]).isEqualTo((byte) 'F');
            assertThat(compressed[3]).isEqualTo((byte) 'F');
        }

        private byte[] generateRandomText(int size) {
            Random random = new Random(42); // Fixed seed for reproducibility
            byte[] data = new byte[size];
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
            for (int i = 0; i < size; i++) {
                data[i] = (byte) chars.charAt(random.nextInt(chars.length()));
            }
            return data;
        }
    }

    @Nested
    @DisplayName("Decompression Tests")
    class DecompressionTests {

        @Test
        @DisplayName("Should throw exception for invalid magic number")
        void decompress_invalidMagicNumber() {
            byte[] invalidData = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

            assertThatThrownBy(() -> codec.decompress(invalidData))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("not a Huffman compressed file");
        }

        @Test
        @DisplayName("Should throw exception for too short data")
        void decompress_tooShort() {
            byte[] shortData = {0x48, 0x55, 0x46};

            assertThatThrownBy(() -> codec.decompress(shortData))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("too short");
        }

        @Test
        @DisplayName("Should throw exception for null input")
        void decompress_nullInput() {
            assertThatThrownBy(() -> codec.decompress(null))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate correct file")
        void isValidCompressedFile_valid() throws IOException {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
            byte[] compressed = codec.compress(data);

            assertThat(codec.isValidCompressedFile(compressed)).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid file")
        void isValidCompressedFile_invalid() {
            byte[] invalid = "Not a huff file".getBytes(StandardCharsets.UTF_8);

            assertThat(codec.isValidCompressedFile(invalid)).isFalse();
        }

        @Test
        @DisplayName("Should reject null")
        void isValidCompressedFile_null() {
            assertThat(codec.isValidCompressedFile(null)).isFalse();
        }

        @Test
        @DisplayName("Should reject empty array")
        void isValidCompressedFile_empty() {
            assertThat(codec.isValidCompressedFile(new byte[0])).isFalse();
        }

        @Test
        @DisplayName("Should reject short array")
        void isValidCompressedFile_short() {
            assertThat(codec.isValidCompressedFile(new byte[3])).isFalse();
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should return correct statistics")
        void getCodeStatistics() {
            byte[] data = "AAAAABBBCC".getBytes(StandardCharsets.UTF_8);

            Map<String, Object> stats = codec.getCodeStatistics(data);

            assertThat(stats.get("uniqueSymbols")).isEqualTo(3);
            assertThat(stats.get("totalSymbols")).isEqualTo(10);
            assertThat((double) stats.get("averageCodeLength")).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle empty data")
        void getCodeStatistics_empty() {
            byte[] data = new byte[0];

            Map<String, Object> stats = codec.getCodeStatistics(data);

            assertThat(stats.get("uniqueSymbols")).isEqualTo(0);
            assertThat(stats.get("totalSymbols")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle binary data")
        void compress_binaryData() throws IOException {
            byte[] data = new byte[256];
            for (int i = 0; i < 256; i++) {
                data[i] = (byte) i;
            }

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
        }

        @Test
        @DisplayName("Should handle null bytes")
        void compress_nullBytes() throws IOException {
            byte[] data = {0, 0, 0, 0, 0};

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
        }

        @Test
        @DisplayName("Should handle negative byte values")
        void compress_negativeBytes() throws IOException {
            byte[] data = {-1, -128, -64, -32, -1};

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
        }

        @Test
        @DisplayName("Should handle maximum byte values")
        void compress_maxByteValues() throws IOException {
            byte[] data = {127, 127, 127, -128, -128, -128};

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(decompressed).isEqualTo(data);
        }

        @ParameterizedTest
        @MethodSource("provideSpecialStrings")
        @DisplayName("Should handle special strings")
        void compress_specialStrings(String input) throws IOException {
            byte[] data = input.getBytes(StandardCharsets.UTF_8);

            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);

            assertThat(new String(decompressed, StandardCharsets.UTF_8)).isEqualTo(input);
        }

        static Stream<Arguments> provideSpecialStrings() {
            return Stream.of(
                    Arguments.of("Hello\nWorld"),
                    Arguments.of("Tab\tSeparated"),
                    Arguments.of("Carriage\rReturn"),
                    Arguments.of("Unicode: こんにちは"),
                    Arguments.of("Emoji: 🎉🚀💻"),
                    Arguments.of("   Spaces   "),
                    Arguments.of("Special!@#$%^&*()"),
                    Arguments.of("\"Quoted\""),
                    Arguments.of("Path/To/File"),
                    Arguments.of("Multiple\n\n\nNewlines")
            );
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should compress large file within reasonable time")
        void compress_largeFile() throws IOException {
            // Generate 1MB of random text
            byte[] data = new byte[1024 * 1024];
            Random random = new Random(42);
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) ('A' + random.nextInt(26));
            }

            long startTime = System.currentTimeMillis();
            byte[] compressed = codec.compress(data);
            byte[] decompressed = codec.decompress(compressed);
            long endTime = System.currentTimeMillis();

            assertThat(decompressed).isEqualTo(data);
            assertThat(endTime - startTime).isLessThan(5000); // Should complete within 5 seconds
        }
    }
}
