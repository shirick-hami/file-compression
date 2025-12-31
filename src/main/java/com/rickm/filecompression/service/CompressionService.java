package com.rickm.filecompression.service;

import com.rickm.filecompression.model.CompressionResult;
import com.rickm.filecompression.model.ProgressInfo;

import java.util.Map;

public interface CompressionService {
    CompressionResult compress(byte[] data, String fileName);
    CompressionResult decompress(byte[] compressedData, String fileName);
    ProgressInfo getProgress(String operationId);
    CompressionResult getResult(String operationId);
    Map<String, Object> analyzeData(byte[] data);
    boolean canDecompress(byte[] data);
    void cleanup(String operationId);
}
