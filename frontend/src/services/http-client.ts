/**
 * HTTP Client - Global Entry Point
 * Provides a centralized fetch wrapper with common headers, base URL, and error handling
 */

import {API_CONFIG, DEFAULT_HEADERS} from '../constants/api.constants';
import {getApiUrl} from "@utils/env.service";

/**
 * Request configuration options
 */
export interface RequestConfig {
    headers?: Record<string, string>;
    timeout?: number;
    credentials?: RequestCredentials;
    signal?: AbortSignal;
}

/**
 * HTTP Response wrapper
 */
export interface HttpResponse<T = unknown> {
    data: T;
    status: number;
    statusText: string;
    headers: Headers;
    ok: boolean;
}

/**
 * HTTP Error class for better error handling
 */
export class HttpError extends Error {
    constructor(
        public status: number,
        public statusText: string,
        public response?: unknown,
        message?: string
    ) {
        super(message || `HTTP Error ${status}: ${statusText}`);
        this.name = 'HttpError';
    }
}

/**
 * HTTP Client Configuration
 */
interface HttpClientConfig {
    baseUrl: string;
    defaultHeaders: Record<string, string>;
    timeout: number;
}

/**
 * Global HTTP Client
 * Singleton class that manages all HTTP requests
 */
class HttpClient {
    private config: HttpClientConfig;

    constructor() {
        this.config = {
            baseUrl: getApiUrl(),
            defaultHeaders: {...DEFAULT_HEADERS},
            timeout: API_CONFIG.TIMEOUT,
        };
    }

    /**
     * Get the current base URL
     */
    getBaseUrl(): string {
        return this.config.baseUrl;
    }

    /**
     * Set a new base URL
     */
    setBaseUrl(url: string): void {
        this.config.baseUrl = url.replace(/\/$/, ''); // Remove trailing slash
    }

    /**
     * Get all default headers
     */
    getDefaultHeaders(): Record<string, string> {
        return {...this.config.defaultHeaders};
    }

    /**
     * Set a default header
     */
    setDefaultHeader(key: string, value: string): void {
        this.config.defaultHeaders[key] = value;
    }

    /**
     * Remove a default header
     */
    removeDefaultHeader(key: string): void {
        delete this.config.defaultHeaders[key];
    }

    /**
     * Set the default timeout
     */
    setTimeout(timeout: number): void {
        this.config.timeout = timeout;
    }

    /**
     * Build the full URL from endpoint
     */
    buildUrl(endpoint: string): string {
        // If endpoint is already a full URL, return as is
        if (endpoint.startsWith('http://') || endpoint.startsWith('https://')) {
            return endpoint;
        }

        // Ensure endpoint starts with /
        const path = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
        return `${this.config.baseUrl}${path}`;
    }

    /**
     * Merge headers with defaults
     */
    private mergeHeaders(customHeaders?: Record<string, string>, isFormData: boolean = false): Headers {
        const headers = new Headers();

        // Add default headers (skip Content-Type for FormData - browser sets it automatically)
        Object.entries(this.config.defaultHeaders).forEach(([key, value]) => {
            if (!(isFormData && key.toLowerCase() === 'content-type')) {
                headers.set(key, value);
            }
        });

        // Add custom headers
        if (customHeaders) {
            Object.entries(customHeaders).forEach(([key, value]) => {
                if (!(isFormData && key.toLowerCase() === 'content-type')) {
                    headers.set(key, value);
                }
            });
        }

        return headers;
    }

    /**
     * Create an abort controller with timeout
     */
    private createAbortController(timeout?: number, existingSignal?: AbortSignal): {
        controller: AbortController;
        timeoutId: NodeJS.Timeout | null;
    } {
        const controller = new AbortController();
        const effectiveTimeout = timeout ?? this.config.timeout;

        // Link to existing signal if provided
        if (existingSignal) {
            existingSignal.addEventListener('abort', () => controller.abort());
        }

        // Set up timeout
        const timeoutId = effectiveTimeout > 0
            ? setTimeout(() => controller.abort(), effectiveTimeout)
            : null;

        return {controller, timeoutId};
    }

    /**
     * Core fetch wrapper
     */
    async fetch<T = unknown>(
        endpoint: string,
        options: RequestInit = {},
        config: RequestConfig = {}
    ): Promise<HttpResponse<T>> {
        const url = this.buildUrl(endpoint);
        const isFormData = options.body instanceof FormData;
        const headers = this.mergeHeaders(config.headers, isFormData);

        const {controller, timeoutId} = this.createAbortController(
            config.timeout,
            config.signal
        );

        try {
            const response = await fetch(url, {
                ...options,
                headers,
                credentials: config.credentials ?? 'same-origin',
                signal: controller.signal,
            });

            // Clear timeout if request completed
            if (timeoutId) {
                clearTimeout(timeoutId);
            }

            // Parse response
            let data: T;
            const contentType = response.headers.get('content-type') || '';

            if (contentType.includes('application/json')) {
                data = await response.json();
            } else if (contentType.includes('text/')) {
                data = await response.text() as unknown as T;
            } else {
                data = await response.blob() as unknown as T;
            }

            // Handle non-OK responses
            if (!response.ok) {
                throw new HttpError(
                    response.status,
                    response.statusText,
                    data,
                    `Request failed: ${response.status} ${response.statusText}`
                );
            }

            return {
                data,
                status: response.status,
                statusText: response.statusText,
                headers: response.headers,
                ok: response.ok,
            };
        } catch (error) {
            // Clear timeout on error
            if (timeoutId) {
                clearTimeout(timeoutId);
            }

            // Handle abort (timeout)
            if (error instanceof Error && error.name === 'AbortError') {
                throw new HttpError(
                    408,
                    'Request Timeout',
                    null,
                    `Request to ${url} timed out after ${config.timeout ?? this.config.timeout}ms`
                );
            }

            // Re-throw HttpError
            if (error instanceof HttpError) {
                throw error;
            }

            // Wrap other errors
            throw new HttpError(
                0,
                'Network Error',
                null,
                error instanceof Error ? error.message : 'An unknown error occurred'
            );
        }
    }
}

// Export singleton instance
export const httpClient = new HttpClient();

// Export class for testing
export {HttpClient};
