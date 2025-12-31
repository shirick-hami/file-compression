/**
 * API Constants
 * Centralized configuration for all API endpoints and settings
 */

// Base URL Configuration
export const API_CONFIG = {
    TIMEOUT: 30000, // 30 seconds
} as const;

// API Endpoints
export const API_ENDPOINTS = {
    // Compression endpoints
    COMPRESS: '/huffman/compress',
    COMPRESS_JSON: '/huffman/compress/json',

    // Decompression endpoints
    DECOMPRESS: '/huffman/decompress',
    DECOMPRESS_JSON: '/huffman/decompress/json',

    // Analysis and validation
    ANALYZE: '/huffman/analyze',
    VALIDATE: '/huffman/validate',

    // Health
    HEALTH: '/huffman/health',
} as const;

// HTTP Headers
export const DEFAULT_HEADERS: Record<string, string> = {
    'Accept': 'application/json',
} as const;

// Content Types
export const CONTENT_TYPES = {
    JSON: 'application/json',
    FORM_DATA: 'multipart/form-data',
    OCTET_STREAM: 'application/octet-stream',
    TEXT_PLAIN: 'text/plain',
} as const;

// HTTP Methods
export const HTTP_METHODS = {
    GET: 'GET',
    POST: 'POST',
    PUT: 'PUT',
    PATCH: 'PATCH',
    DELETE: 'DELETE',
} as const;

// External Links (for documentation)
export const EXTERNAL_LINKS = {
    MIT_LICENSE: 'https://opensource.org/licenses/MIT',
} as const;

// Type exports for type safety
export type ApiEndpoint = typeof API_ENDPOINTS[keyof typeof API_ENDPOINTS];
export type HttpMethod = typeof HTTP_METHODS[keyof typeof HTTP_METHODS];
export type ContentType = typeof CONTENT_TYPES[keyof typeof CONTENT_TYPES];
