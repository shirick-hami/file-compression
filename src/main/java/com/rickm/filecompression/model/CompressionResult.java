package com.rickm.filecompression.model;

/**
 * Represents the result of a compression or decompression operation.
 */
public class CompressionResult {
    
    private final String operationId;
    private final String fileName;
    private final long originalSize;
    private final long processedSize;
    private final double compressionRatio;
    private final long processingTimeMs;
    private final OperationType operationType;
    private final boolean success;
    private final String errorMessage;
    private final byte[] data;
    
    public enum OperationType {
        COMPRESS, DECOMPRESS
    }
    
    private CompressionResult(Builder builder) {
        this.operationId = builder.operationId;
        this.fileName = builder.fileName;
        this.originalSize = builder.originalSize;
        this.processedSize = builder.processedSize;
        this.compressionRatio = builder.compressionRatio;
        this.processingTimeMs = builder.processingTimeMs;
        this.operationType = builder.operationType;
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
        this.data = builder.data;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getOperationId() { return operationId; }
    public String getFileName() { return fileName; }
    public long getOriginalSize() { return originalSize; }
    public long getProcessedSize() { return processedSize; }
    public double getCompressionRatio() { return compressionRatio; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public OperationType getOperationType() { return operationType; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    public byte[] getData() { return data; }
    
    public String getFormattedOriginalSize() {
        return formatSize(originalSize);
    }
    
    public String getFormattedProcessedSize() {
        return formatSize(processedSize);
    }
    
    public String getFormattedCompressionRatio() {
        return String.format("%.2f%%", compressionRatio * 100);
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    public static class Builder {
        private String operationId;
        private String fileName;
        private long originalSize;
        private long processedSize;
        private double compressionRatio;
        private long processingTimeMs;
        private OperationType operationType;
        private boolean success = true;
        private String errorMessage;
        private byte[] data;
        
        public Builder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }
        
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        
        public Builder originalSize(long originalSize) {
            this.originalSize = originalSize;
            return this;
        }
        
        public Builder processedSize(long processedSize) {
            this.processedSize = processedSize;
            return this;
        }
        
        public Builder compressionRatio(double compressionRatio) {
            this.compressionRatio = compressionRatio;
            return this;
        }
        
        public Builder processingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
            return this;
        }
        
        public Builder operationType(OperationType operationType) {
            this.operationType = operationType;
            return this;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }
        
        public CompressionResult build() {
            return new CompressionResult(this);
        }
    }
}
