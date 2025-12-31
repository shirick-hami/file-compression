package com.rickm.filecompression.service;

import com.rickm.filecompression.model.CompressionResult;
import com.rickm.filecompression.model.ProgressInfo;
import com.rickm.filecompression.service.impl.HuffmanCompressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test suite for HuffmanCompressionService.
 */
@DisplayName("HuffmanCompressionService Tests")
class HuffmanCompressionServiceTest {

    private HuffmanCompressionService compressionService;

    @BeforeEach
    void setUp() {
        compressionService = new HuffmanCompressionService();
    }

    @Nested
    @DisplayName("Compression Tests")
    class CompressionTests {

        @Test
        @DisplayName("Should compress text successfully")
        void compress_textFile_success() {
            String text = "Hello, World! This is a test of Huffman compression.";
            byte[] data = text.getBytes(StandardCharsets.UTF_8);

            CompressionResult result = compressionService.compress(data, "test.txt");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getFileName()).isEqualTo("test.txt.huff");
            assertThat(result.getOriginalSize()).isEqualTo(data.length);
            assertThat(result.getProcessedSize()).isGreaterThan(0);
            assertThat(result.getData()).isNotNull();
            assertThat(result.getOperationType()).isEqualTo(CompressionResult.OperationType.COMPRESS);
            assertThat(result.getOperationId()).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty input")
        void compress_emptyInput_success() {
            byte[] data = new byte[0];

            CompressionResult result = compressionService.compress(data, "empty.txt");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getOriginalSize()).isZero();
        }

        @Test
        @DisplayName("Should calculate compression ratio correctly")
        void compress_calculatesCompressionRatio() {
            // Create highly compressible data
            byte[] data = new byte[1000];
            Arrays.fill(data, (byte) 'A');

            CompressionResult result = compressionService.compress(data, "repetitive.txt");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getCompressionRatio()).isGreaterThan(0);
            assertThat(result.getFormattedCompressionRatio()).contains("%");
        }

        @Test
        @DisplayName("Should track processing time")
        void compress_tracksProcessingTime() {
            byte[] data = "Test data for timing".getBytes(StandardCharsets.UTF_8);

            CompressionResult result = compressionService.compress(data, "test.txt");

            assertThat(result.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should format sizes correctly")
        void compress_formatsSizesCorrectly() {
            byte[] data = new byte[1500];
            Arrays.fill(data, (byte) 'X');

            CompressionResult result = compressionService.compress(data, "test.txt");

            assertThat(result.getFormattedOriginalSize()).contains("KB");
        }

        @Test
        @DisplayName("Should generate unique operation IDs")
        void compress_generatesUniqueOperationIds() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);

            CompressionResult result1 = compressionService.compress(data, "test1.txt");
            CompressionResult result2 = compressionService.compress(data, "test2.txt");

            assertThat(result1.getOperationId()).isNotEqualTo(result2.getOperationId());
        }

        @Test
        @DisplayName("Should handle binary data")
        void compress_binaryData_success() {
            byte[] data = new byte[256];
            for (int i = 0; i < 256; i++) {
                data[i] = (byte) i;
            }

            CompressionResult result = compressionService.compress(data, "binary.bin");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @Test
        @DisplayName("Should handle large files")
        void compress_largeFile_success() {
            byte[] data = new byte[100000];
            Arrays.fill(data, (byte) 'A');

            CompressionResult result = compressionService.compress(data, "large.txt");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getProcessedSize()).isLessThan(data.length);
        }
    }

    @Nested
    @DisplayName("Decompression Tests")
    class DecompressionTests {

        @Test
        @DisplayName("Should decompress successfully")
        void decompress_validFile_success() {
            String original = "Hello, World!";
            byte[] data = original.getBytes(StandardCharsets.UTF_8);

            CompressionResult compressResult = compressionService.compress(data, "test.txt");
            CompressionResult decompressResult = compressionService.decompress(
                    compressResult.getData(), "test.txt.huff");

            assertThat(decompressResult.isSuccess()).isTrue();
            assertThat(decompressResult.getData()).isEqualTo(data);
            assertThat(decompressResult.getOperationType())
                    .isEqualTo(CompressionResult.OperationType.DECOMPRESS);
        }

        @Test
        @DisplayName("Should reject invalid file")
        void decompress_invalidFile_failure() {
            byte[] invalidData = "Not a huff file".getBytes(StandardCharsets.UTF_8);

            CompressionResult result = compressionService.decompress(invalidData, "invalid.txt");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Invalid file format");
        }

        @Test
        @DisplayName("Should remove .huff extension from output filename")
        void decompress_removesHuffExtension() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
            CompressionResult compressResult = compressionService.compress(data, "test.txt");

            CompressionResult decompressResult = compressionService.decompress(
                    compressResult.getData(), "test.txt.huff");

            assertThat(decompressResult.getFileName()).isEqualTo("test.txt");
        }

        @Test
        @DisplayName("Should add prefix if no .huff extension")
        void decompress_addsPrefixWithoutHuffExtension() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
            CompressionResult compressResult = compressionService.compress(data, "test.txt");

            CompressionResult decompressResult = compressionService.decompress(
                    compressResult.getData(), "compressed_file");

            assertThat(decompressResult.getFileName()).startsWith("decompressed_");
        }

        @Test
        @DisplayName("Should handle empty compressed file")
        void decompress_emptyFile_success() {
            byte[] data = new byte[0];
            CompressionResult compressResult = compressionService.compress(data, "empty.txt");

            CompressionResult decompressResult = compressionService.decompress(
                    compressResult.getData(), "empty.txt.huff");

            assertThat(decompressResult.isSuccess()).isTrue();
            assertThat(decompressResult.getData()).isEmpty();
        }

        @Test
        @DisplayName("Should preserve binary data through round trip")
        void decompress_preservesBinaryData() {
            byte[] data = new byte[256];
            for (int i = 0; i < 256; i++) {
                data[i] = (byte) i;
            }

            CompressionResult compressResult = compressionService.compress(data, "binary.bin");
            CompressionResult decompressResult = compressionService.decompress(
                    compressResult.getData(), "binary.bin.huff");

            assertThat(decompressResult.isSuccess()).isTrue();
            assertThat(decompressResult.getData()).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("Progress Tracking Tests")
    class ProgressTrackingTests {

        @Test
        @DisplayName("Should track compression progress")
        void compress_tracksProgress() {
            byte[] data = new byte[10000];
            Arrays.fill(data, (byte) 'A');

            CompressionResult result = compressionService.compress(data, "test.txt");
            ProgressInfo progress = compressionService.getProgress(result.getOperationId());

            assertThat(progress).isNotNull();
            assertThat(progress.getOperationId()).isEqualTo(result.getOperationId());
            assertThat(progress.getStatus()).isEqualTo(ProgressInfo.OperationStatus.COMPLETED);
            assertThat(progress.getProgressPercent()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should return null for unknown operation ID")
        void getProgress_unknownId_returnsNull() {
            ProgressInfo progress = compressionService.getProgress("unknown-id");

            assertThat(progress).isNull();
        }

        @Test
        @DisplayName("Should track decompression progress")
        void decompress_tracksProgress() {
            byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);
            CompressionResult compressResult = compressionService.compress(data, "test.txt");

            CompressionResult decompressResult = compressionService.decompress(
                    compressResult.getData(), "test.txt.huff");
            ProgressInfo progress = compressionService.getProgress(decompressResult.getOperationId());

            assertThat(progress).isNotNull();
            assertThat(progress.getStatus()).isEqualTo(ProgressInfo.OperationStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should update progress timestamps")
        void compress_updatesTimestamps() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);

            CompressionResult result = compressionService.compress(data, "test.txt");
            ProgressInfo progress = compressionService.getProgress(result.getOperationId());

            assertThat(progress.getStartTime()).isNotNull();
            assertThat(progress.getLastUpdated()).isNotNull();
            assertThat(progress.getLastUpdated()).isAfterOrEqualTo(progress.getStartTime());
        }
    }

    @Nested
    @DisplayName("Result Caching Tests")
    class ResultCachingTests {

        @Test
        @DisplayName("Should cache compression result")
        void compress_cachesResult() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);

            CompressionResult result = compressionService.compress(data, "test.txt");
            CompressionResult cached = compressionService.getResult(result.getOperationId());

            assertThat(cached).isNotNull();
            assertThat(cached.getOperationId()).isEqualTo(result.getOperationId());
        }

        @Test
        @DisplayName("Should return null for unknown result")
        void getResult_unknownId_returnsNull() {
            CompressionResult result = compressionService.getResult("unknown-id");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should cleanup cached data")
        void cleanup_removesCachedData() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
            CompressionResult result = compressionService.compress(data, "test.txt");
            String operationId = result.getOperationId();

            compressionService.cleanup(operationId);

            assertThat(compressionService.getProgress(operationId)).isNull();
            assertThat(compressionService.getResult(operationId)).isNull();
        }
    }

    @Nested
    @DisplayName("Analysis Tests")
    class AnalysisTests {

        @Test
        @DisplayName("Should analyze data correctly")
        void analyzeData_returnsStatistics() {
            byte[] data = "AAAAABBBCC".getBytes(StandardCharsets.UTF_8);

            Map<String, Object> stats = compressionService.analyzeData(data);

            assertThat(stats).containsKeys(
                    "uniqueSymbols",
                    "totalSymbols",
                    "averageCodeLength",
                    "maxCodeLength",
                    "minCodeLength",
                    "theoreticalCompression"
            );
            assertThat((int) stats.get("uniqueSymbols")).isEqualTo(3);
            assertThat((int) stats.get("totalSymbols")).isEqualTo(10);
        }

        @Test
        @DisplayName("Should handle empty data analysis")
        void analyzeData_emptyData() {
            byte[] data = new byte[0];

            Map<String, Object> stats = compressionService.analyzeData(data);

            assertThat((int) stats.get("uniqueSymbols")).isZero();
            assertThat((int) stats.get("totalSymbols")).isZero();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate compressed file")
        void canDecompress_validFile_returnsTrue() {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
            CompressionResult result = compressionService.compress(data, "test.txt");

            boolean canDecompress = compressionService.canDecompress(result.getData());

            assertThat(canDecompress).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid file")
        void canDecompress_invalidFile_returnsFalse() {
            byte[] data = "Not compressed".getBytes(StandardCharsets.UTF_8);

            boolean canDecompress = compressionService.canDecompress(data);

            assertThat(canDecompress).isFalse();
        }

        @Test
        @DisplayName("Should reject null")
        void canDecompress_null_returnsFalse() {
            boolean canDecompress = compressionService.canDecompress(null);

            assertThat(canDecompress).isFalse();
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data through multiple round trips")
        void multipleRoundTrips_preservesData() {
            String original = "The quick brown fox jumps over the lazy dog.";
            byte[] data = original.getBytes(StandardCharsets.UTF_8);

            // First round trip
            CompressionResult compress1 = compressionService.compress(data, "test.txt");
            CompressionResult decompress1 = compressionService.decompress(
                    compress1.getData(), "test.txt.huff");

            // Second round trip
            CompressionResult compress2 = compressionService.compress(
                    decompress1.getData(), "test.txt");
            CompressionResult decompress2 = compressionService.decompress(
                    compress2.getData(), "test.txt.huff");

            assertThat(decompress2.getData()).isEqualTo(data);
            assertThat(new String(decompress2.getData(), StandardCharsets.UTF_8))
                    .isEqualTo(original);
        }

        @Test
        @DisplayName("Should handle Unicode through round trip")
        void roundTrip_unicode() {
            String original = "日本語テスト 🎉 émojis and spëcial çharacters";
            byte[] data = original.getBytes(StandardCharsets.UTF_8);

            CompressionResult compressResult = compressionService.compress(data, "unicode.txt");
            CompressionResult decompressResult = compressionService.decompress(
                    compressResult.getData(), "unicode.txt.huff");

            assertThat(new String(decompressResult.getData(), StandardCharsets.UTF_8))
                    .isEqualTo(original);
        }
    }
}
