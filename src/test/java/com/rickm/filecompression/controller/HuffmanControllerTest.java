package com.rickm.filecompression.controller;

import com.rickm.filecompression.service.impl.HuffmanCompressionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HuffmanCompressionController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("HuffmanController Integration Tests")
class HuffmanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HuffmanCompressionService compressionService;

    @Nested
    @DisplayName("Compress Endpoint Tests")
    class CompressEndpointTests {

        @Test
        @DisplayName("POST /huffman/compress should compress file successfully")
        void compress_validFile_returnsCompressedData() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Hello, World! This is test data for compression.".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/huffman/compress").file(file))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Operation-Id"))
                    .andExpect(header().exists("X-Original-Size"))
                    .andExpect(header().exists("X-Compressed-Size"))
                    .andExpect(header().exists("X-Compression-Ratio"))
                    .andExpect(header().string("Content-Disposition", containsString("test.txt.huff")));
        }

        @Test
        @DisplayName("POST /api/compress should reject empty file")
        void compress_emptyFile_returnsBadRequest() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    new byte[0]
            );

            mockMvc.perform(multipart("/huffman/compress").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /api/compress/json should return JSON with base64 data")
        void compressJson_validFile_returnsJsonResponse() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Test data".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/huffman/compress/json").file(file))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.operationId").exists())
                    .andExpect(jsonPath("$.fileName").value("test.txt.huff"))
                    .andExpect(jsonPath("$.originalSize").isNumber())
                    .andExpect(jsonPath("$.compressedSize").isNumber())
                    .andExpect(jsonPath("$.compressionRatio").isNumber())
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("POST /api/compress should handle large files")
        void compress_largeFile_succeeds() throws Exception {
            byte[] largeData = new byte[100000];
            Arrays.fill(largeData, (byte) 'A');

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "large.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    largeData
            );

            mockMvc.perform(multipart("/huffman/compress").file(file))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Compression-Ratio"));
        }

        @Test
        @DisplayName("POST /api/compress should handle binary files")
        void compress_binaryFile_succeeds() throws Exception {
            byte[] binaryData = new byte[256];
            for (int i = 0; i < 256; i++) {
                binaryData[i] = (byte) i;
            }

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "binary.bin",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    binaryData
            );

            mockMvc.perform(multipart("/huffman/compress").file(file))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Decompress Endpoint Tests")
    class DecompressEndpointTests {

        @Test
        @DisplayName("POST /api/decompress should decompress valid file")
        void decompress_validFile_returnsOriginalData() throws Exception {
            // First compress some data
            String original = "Test data for decompression";
            byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
            var compressResult = compressionService.compress(originalBytes, "test.txt");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt.huff",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    compressResult.getData()
            );

            MvcResult result = mockMvc.perform(multipart("/huffman/decompress").file(file))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Operation-Id"))
                    .andReturn();

            assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(originalBytes);
        }

        @Test
        @DisplayName("POST /api/decompress should reject invalid file")
        void decompress_invalidFile_returnsBadRequest() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "invalid.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Not a compressed file".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/huffman/decompress").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value(containsString("Invalid file format")));
        }

        @Test
        @DisplayName("POST /api/decompress/json should return JSON response")
        void decompressJson_validFile_returnsJsonResponse() throws Exception {
            String original = "Test data";
            byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
            var compressResult = compressionService.compress(originalBytes, "test.txt");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt.huff",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    compressResult.getData()
            );

            mockMvc.perform(multipart("/huffman/decompress/json").file(file))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.fileName").value("test.txt"))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("POST /api/decompress should reject empty file")
        void decompress_emptyFile_returnsBadRequest() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.huff",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    new byte[0]
            );

            mockMvc.perform(multipart("/huffman/decompress").file(file))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Analyze Endpoint Tests")
    class AnalyzeEndpointTests {

        @Test
        @DisplayName("POST /api/analyze should return statistics")
        void analyze_validFile_returnsStatistics() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "AAAAABBBCC".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/huffman/analyze").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uniqueSymbols").value(3))
                    .andExpect(jsonPath("$.totalSymbols").value(10))
                    .andExpect(jsonPath("$.averageCodeLength").isNumber())
                    .andExpect(jsonPath("$.fileName").value("test.txt"))
                    .andExpect(jsonPath("$.fileSize").value(10));
        }

        @Test
        @DisplayName("POST /api/analyze should reject empty file")
        void analyze_emptyFile_returnsBadRequest() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    new byte[0]
            );

            mockMvc.perform(multipart("/huffman/analyze").file(file))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Validate Endpoint Tests")
    class ValidateEndpointTests {

        @Test
        @DisplayName("POST /api/validate should validate compressed file")
        void validate_compressedFile_returnsValid() throws Exception {
            byte[] data = "Test".getBytes(StandardCharsets.UTF_8);
            var compressResult = compressionService.compress(data, "test.txt");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.huff",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    compressResult.getData()
            );

            mockMvc.perform(multipart("/huffman/validate").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.message").value(containsString("valid")));
        }

        @Test
        @DisplayName("POST /api/validate should reject invalid file")
        void validate_regularFile_returnsInvalid() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Not compressed".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/huffman/validate").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("not")));
        }
    }

    @Nested
    @DisplayName("Health Endpoint Tests")
    class HealthEndpointTests {

        @Test
        @DisplayName("GET /api/health should return UP status")
        void health_returnsUpStatus() throws Exception {
            mockMvc.perform(get("/huffman/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.service").value("Huffman Compressor"))
                    .andExpect(jsonPath("$.version").value("1.0.0"));
        }
    }

    @Nested
    @DisplayName("Round Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Compress and decompress should preserve data")
        void compressDecompress_preservesData() throws Exception {
            String original = "The quick brown fox jumps over the lazy dog. 日本語 🎉";
            byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);

            // Compress
            MockMultipartFile compressFile = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    originalBytes
            );

            MvcResult compressResult = mockMvc.perform(multipart("/huffman/compress").file(compressFile))
                    .andExpect(status().isOk())
                    .andReturn();

            byte[] compressedData = compressResult.getResponse().getContentAsByteArray();

            // Decompress
            MockMultipartFile decompressFile = new MockMultipartFile(
                    "file",
                    "test.txt.huff",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    compressedData
            );

            MvcResult decompressResult = mockMvc.perform(multipart("/huffman/decompress").file(decompressFile))
                    .andExpect(status().isOk())
                    .andReturn();

            byte[] decompressedData = decompressResult.getResponse().getContentAsByteArray();

            assertThat(decompressedData).isEqualTo(originalBytes);
            assertThat(new String(decompressedData, StandardCharsets.UTF_8)).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle missing file parameter")
        void compress_missingFile_returnsBadRequest() throws Exception {
            mockMvc.perform(multipart("/huffman/compress"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return appropriate error for corrupted compressed data")
        void decompress_corruptedData_returnsError() throws Exception {
            // Create data that looks like a huff file but is corrupted
            byte[] corruptedData = new byte[20];
            corruptedData[0] = 'H';
            corruptedData[1] = 'U';
            corruptedData[2] = 'F';
            corruptedData[3] = 'F';
            // Rest is garbage

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "corrupted.huff",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    corruptedData
            );

            mockMvc.perform(multipart("/huffman/decompress").file(file))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("CORS Tests")
    class CorsTests {

        @Test
        @DisplayName("Should include CORS headers")
        void compress_includesCorsHeaders() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "Test".getBytes(StandardCharsets.UTF_8)
            );

            mockMvc.perform(multipart("/huffman/compress")
                            .file(file)
                            .header("Origin", "http://localhost:3333"))
                    .andExpect(status().isOk());
        }
    }
}
