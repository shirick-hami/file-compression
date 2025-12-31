/**
 * API Service for Huffman Compressor
 * Handles all communication with the Spring Boot backend
 * Uses the centralized HTTP service for all requests
 */

import {httpService, HttpError} from './http.service';
import {API_ENDPOINTS} from '../constants/api.constants';

export interface CompressionResult {
    success: boolean;
    data?: string;
    fileName?: string;
    originalSize?: number;
    compressedSize?: number;
    formattedOriginalSize?: string;
    formattedCompressedSize?: string;
    formattedCompressionRatio?: string;
    processingTimeMs?: number;
    error?: string;
    errorMessage?: string;
}

export interface DecompressionResult {
    success: boolean;
    data?: string;
    fileName?: string;
    originalSize?: number;
    compressedSize?: number;
    formattedOriginalSize?: string;
    formattedCompressedSize?: string;
    processingTimeMs?: number;
    error?: string;
    errorMessage?: string;
}

export interface AnalysisResult {
    success: boolean;
    originalSize?: number;
    estimatedCompressedSize?: number;
    estimatedRatio?: string;
    uniqueSymbols?: number;
    error?: string;
}

export interface ValidationResult {
    valid: boolean;
    message?: string;
}

export interface HealthResult {
    status: string;
    timestamp?: string;
}

class ApiService {
    /**
     * Set the base URL for the API
     */
    setBaseUrl(url: string): void {
        httpService.setBaseUrl(url);
    }

    /**
     * Get the current base URL
     */
    getBaseUrl(): string {
        return httpService.getBaseUrl();
    }

    /**
     * Compress a file
     */
    async compress(file: File): Promise<CompressionResult> {
        try {
            const response = await httpService.uploadFile<CompressionResult>(
                API_ENDPOINTS.COMPRESS_JSON,
                file
            );
            return response.data;
        } catch (error) {
            return this.handleError<CompressionResult>(error, 'Compression failed');
        }
    }

    /**
     * Compress a file and get binary response
     */
    async compressBinary(file: File): Promise<Blob | null> {
        try {
            const response = await httpService.uploadFile<Blob>(
                API_ENDPOINTS.COMPRESS,
                file
            );
            return response.data;
        } catch (error) {
            console.error('Compression failed:', error);
            return null;
        }
    }

    /**
     * Decompress a .huff file
     */
    async decompress(file: File): Promise<DecompressionResult> {
        try {
            const response = await httpService.uploadFile<DecompressionResult>(
                API_ENDPOINTS.DECOMPRESS_JSON,
                file
            );
            return response.data;
        } catch (error) {
            return this.handleError<DecompressionResult>(error, 'Decompression failed');
        }
    }

    /**
     * Decompress a .huff file and get binary response
     */
    async decompressBinary(file: File): Promise<Blob | null> {
        try {
            const response = await httpService.uploadFile<Blob>(
                API_ENDPOINTS.DECOMPRESS,
                file
            );
            return response.data;
        } catch (error) {
            console.error('Decompression failed:', error);
            return null;
        }
    }

    /**
     * Analyze a file for compression potential
     */
    async analyze(file: File): Promise<AnalysisResult> {
        try {
            const response = await httpService.uploadFile<AnalysisResult>(
                API_ENDPOINTS.ANALYZE,
                file
            );
            return response.data;
        } catch (error) {
            return this.handleError<AnalysisResult>(error, 'Analysis failed');
        }
    }

    /**
     * Validate if a file is a valid .huff file
     */
    async validate(file: File): Promise<ValidationResult> {
        try {
            const response = await httpService.uploadFile<ValidationResult>(
                API_ENDPOINTS.VALIDATE,
                file
            );
            return response.data;
        } catch (error) {
            if (error instanceof HttpError) {
                return {
                    valid: false,
                    message: error.message,
                };
            }
            return {
                valid: false,
                message: error instanceof Error ? error.message : 'Validation failed',
            };
        }
    }

    /**
     * Check API health
     */
    async health(): Promise<HealthResult> {
        try {
            const response = await httpService.get<HealthResult>(API_ENDPOINTS.HEALTH);
            return response.data;
        } catch (error) {
            return {
                status: 'DOWN',
            };
        }
    }

    /**
     * Check API status (alias for health)
     */
    async status(): Promise<HealthResult> {
        return this.health();
    }

    /**
     * Generic error handler
     */
    private handleError<T extends { success?: boolean; valid?: boolean; error?: string }>(
        error: unknown,
        defaultMessage: string
    ): T {
        let errorMessage = defaultMessage;

        if (error instanceof HttpError) {
            errorMessage = error.message;
        } else if (error instanceof Error) {
            errorMessage = error.message;
        }

        // Return appropriate error structure based on expected type
        return {
            success: false,
            error: errorMessage,
        } as T;
    }
}

// Export singleton instance
export const apiService = new ApiService();

// Also export class for testing
export {ApiService};
