package com.rickm.filecompression.controller;

import com.rickm.filecompression.model.CompressionResult;
import com.rickm.filecompression.model.ProgressInfo;
import com.rickm.filecompression.service.CompressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API controller for compression and decompression operations.
 */
@RestController
@RequestMapping("/huffman")
public class HuffmanController {

    private final CompressionService compressionService;

    @Autowired
    public HuffmanController(CompressionService compressionService) {
        this.compressionService = compressionService;
    }

    /**
     * Compresses an uploaded file.
     */
    @PostMapping("/compress")
    public ResponseEntity<?> compressFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file uploaded"));
        }

        try {
            byte[] data = file.getBytes();
            String fileName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "file";

            CompressionResult result = compressionService.compress(data, fileName);

            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(result.getErrorMessage()));
            }

            // Return compressed file as download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", result.getFileName());
            headers.set("X-Operation-Id", result.getOperationId());
            headers.set("X-Original-Size", String.valueOf(result.getOriginalSize()));
            headers.set("X-Compressed-Size", String.valueOf(result.getProcessedSize()));
            headers.set("X-Compression-Ratio", result.getFormattedCompressionRatio());
            headers.set("X-Processing-Time", String.valueOf(result.getProcessingTimeMs()));

            return new ResponseEntity<>(result.getData(), headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to read uploaded file: " + e.getMessage()));
        }
    }

    /**
     * Decompresses an uploaded Huffman compressed file.
     */
    @PostMapping("/decompress")
    public ResponseEntity<?> decompressFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file uploaded"));
        }

        try {
            byte[] data = file.getBytes();
            String fileName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "file.huff";

            // Check if file can be decompressed
            if (!compressionService.canDecompress(data)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid file format: not a Huffman compressed file (.huff)"));
            }

            CompressionResult result = compressionService.decompress(data, fileName);

            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse(result.getErrorMessage()));
            }

            // Return decompressed file as download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", result.getFileName());
            headers.set("X-Operation-Id", result.getOperationId());
            headers.set("X-Compressed-Size", String.valueOf(result.getOriginalSize()));
            headers.set("X-Original-Size", String.valueOf(result.getProcessedSize()));
            headers.set("X-Processing-Time", String.valueOf(result.getProcessingTimeMs()));

            return new ResponseEntity<>(result.getData(), headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to read uploaded file: " + e.getMessage()));
        }
    }

    /**
     * Compresses a file and returns JSON metadata (file data as base64).
     */
    @PostMapping("/compress/json")
    public ResponseEntity<?> compressFileJson(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file uploaded"));
        }

        try {
            byte[] data = file.getBytes();
            String fileName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "file";

            CompressionResult result = compressionService.compress(data, fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("operationId", result.getOperationId());
            response.put("fileName", result.getFileName());
            response.put("originalSize", result.getOriginalSize());
            response.put("compressedSize", result.getProcessedSize());
            response.put("compressionRatio", result.getCompressionRatio());
            response.put("formattedCompressionRatio", result.getFormattedCompressionRatio());
            response.put("formattedOriginalSize", result.getFormattedOriginalSize());
            response.put("formattedCompressedSize", result.getFormattedProcessedSize());
            response.put("processingTimeMs", result.getProcessingTimeMs());

            if (result.isSuccess()) {
                response.put("data", java.util.Base64.getEncoder().encodeToString(result.getData()));
            } else {
                response.put("errorMessage", result.getErrorMessage());
            }

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to read uploaded file: " + e.getMessage()));
        }
    }

    /**
     * Decompresses a file and returns JSON metadata.
     */
    @PostMapping("/decompress/json")
    public ResponseEntity<?> decompressFileJson(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file uploaded"));
        }

        try {
            byte[] data = file.getBytes();
            String fileName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "file.huff";

            if (!compressionService.canDecompress(data)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid file format: not a Huffman compressed file (.huff)"));
            }

            CompressionResult result = compressionService.decompress(data, fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("operationId", result.getOperationId());
            response.put("fileName", result.getFileName());
            response.put("compressedSize", result.getOriginalSize());
            response.put("originalSize", result.getProcessedSize());
            response.put("formattedCompressedSize", result.getFormattedOriginalSize());
            response.put("formattedOriginalSize", result.getFormattedProcessedSize());
            response.put("processingTimeMs", result.getProcessingTimeMs());

            if (result.isSuccess()) {
                response.put("data", java.util.Base64.getEncoder().encodeToString(result.getData()));
            } else {
                response.put("errorMessage", result.getErrorMessage());
            }

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to read uploaded file: " + e.getMessage()));
        }
    }

    /**
     * Analyzes a file and returns compression statistics.
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file uploaded"));
        }

        try {
            byte[] data = file.getBytes();
            Map<String, Object> stats = compressionService.analyzeData(data);

            stats.put("fileName", file.getOriginalFilename());
            stats.put("fileSize", data.length);
            stats.put("formattedFileSize", formatSize(data.length));

            return ResponseEntity.ok(stats);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to read uploaded file: " + e.getMessage()));
        }
    }

    /**
     * Validates if a file can be decompressed.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("No file uploaded"));
        }

        try {
            byte[] data = file.getBytes();
            boolean isValid = compressionService.canDecompress(data);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("fileName", file.getOriginalFilename());
            response.put("message", isValid
                    ? "File is a valid Huffman compressed file"
                    : "File is not a Huffman compressed file");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to read uploaded file: " + e.getMessage()));
        }
    }

    /**
     * Gets the progress of an operation.
     */
    @GetMapping("/progress/{operationId}")
    public ResponseEntity<?> getProgress(@PathVariable String operationId) {
        ProgressInfo progress = compressionService.getProgress(operationId);

        if (progress == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("operationId", progress.getOperationId());
        response.put("fileName", progress.getFileName());
        response.put("status", progress.getStatus().name());
        response.put("progressPercent", progress.getProgressPercent());
        response.put("currentPhase", progress.getCurrentPhase());
        response.put("bytesProcessed", progress.getBytesProcessed());
        response.put("totalBytes", progress.getTotalBytes());
        response.put("complete", progress.isComplete());

        if (progress.getErrorMessage() != null) {
            response.put("errorMessage", progress.getErrorMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "File Compression Service");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
