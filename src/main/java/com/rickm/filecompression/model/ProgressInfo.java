package com.rickm.filecompression.model;

import java.time.Instant;

/**
 * Represents the progress of an ongoing compression or decompression operation.
 */
public class ProgressInfo {
    
    private final String operationId;
    private final String fileName;
    private final OperationStatus status;
    private final int progressPercent;
    private final String currentPhase;
    private final long bytesProcessed;
    private final long totalBytes;
    private final Instant startTime;
    private final Instant lastUpdated;
    private final String errorMessage;
    
    public enum OperationStatus {
        QUEUED,
        READING_FILE,
        BUILDING_FREQUENCY_TABLE,
        BUILDING_HUFFMAN_TREE,
        GENERATING_CODES,
        ENCODING,
        DECODING,
        WRITING_OUTPUT,
        COMPLETED,
        FAILED
    }
    
    private ProgressInfo(Builder builder) {
        this.operationId = builder.operationId;
        this.fileName = builder.fileName;
        this.status = builder.status;
        this.progressPercent = builder.progressPercent;
        this.currentPhase = builder.currentPhase;
        this.bytesProcessed = builder.bytesProcessed;
        this.totalBytes = builder.totalBytes;
        this.startTime = builder.startTime;
        this.lastUpdated = builder.lastUpdated;
        this.errorMessage = builder.errorMessage;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getOperationId() { return operationId; }
    public String getFileName() { return fileName; }
    public OperationStatus getStatus() { return status; }
    public int getProgressPercent() { return progressPercent; }
    public String getCurrentPhase() { return currentPhase; }
    public long getBytesProcessed() { return bytesProcessed; }
    public long getTotalBytes() { return totalBytes; }
    public Instant getStartTime() { return startTime; }
    public Instant getLastUpdated() { return lastUpdated; }
    public String getErrorMessage() { return errorMessage; }
    
    public boolean isComplete() {
        return status == OperationStatus.COMPLETED || status == OperationStatus.FAILED;
    }
    
    public static class Builder {
        private String operationId;
        private String fileName;
        private OperationStatus status = OperationStatus.QUEUED;
        private int progressPercent = 0;
        private String currentPhase = "Initializing";
        private long bytesProcessed = 0;
        private long totalBytes = 0;
        private Instant startTime = Instant.now();
        private Instant lastUpdated = Instant.now();
        private String errorMessage;
        
        public Builder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }
        
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        
        public Builder status(OperationStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder progressPercent(int progressPercent) {
            this.progressPercent = Math.min(100, Math.max(0, progressPercent));
            return this;
        }
        
        public Builder currentPhase(String currentPhase) {
            this.currentPhase = currentPhase;
            return this;
        }
        
        public Builder bytesProcessed(long bytesProcessed) {
            this.bytesProcessed = bytesProcessed;
            return this;
        }
        
        public Builder totalBytes(long totalBytes) {
            this.totalBytes = totalBytes;
            return this;
        }
        
        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder lastUpdated(Instant lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public ProgressInfo build() {
            return new ProgressInfo(this);
        }
    }
}
