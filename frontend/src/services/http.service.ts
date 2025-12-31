/**
 * HTTP Service
 * High-level HTTP service with typed methods for GET, POST, PUT, PATCH, DELETE
 * Uses the global HttpClient for all requests
 */

import {httpClient, HttpResponse, RequestConfig, HttpError} from './http-client';
import {HTTP_METHODS, CONTENT_TYPES} from '../constants/api.constants';

/**
 * Request options for HTTP methods
 */
export interface HttpRequestOptions extends RequestConfig {
    params?: Record<string, string | number | boolean>;
}

/**
 * POST/PUT/PATCH body options
 */
export interface HttpBodyOptions extends HttpRequestOptions {
    contentType?: 'json' | 'form-data' | 'text';
}

/**
 * HTTP Service Class
 * Provides overloaded methods for all HTTP operations
 */
class HttpService {
    /**
     * Build query string from params object
     */
    private buildQueryString(params?: Record<string, string | number | boolean>): string {
        if (!params || Object.keys(params).length === 0) {
            return '';
        }

        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null) {
                searchParams.append(key, String(value));
            }
        });

        return `?${searchParams.toString()}`;
    }

    /**
     * Build endpoint with query parameters
     */
    private buildEndpoint(endpoint: string, params?: Record<string, string | number | boolean>): string {
        return `${endpoint}${this.buildQueryString(params)}`;
    }

    /**
     * Prepare body and headers based on content type
     */
    private prepareBody(
        body: unknown,
        contentType: 'json' | 'form-data' | 'text' = 'json'
    ): { body: BodyInit; headers: Record<string, string> } {
        switch (contentType) {
            case 'form-data':
                // FormData - browser will set Content-Type with boundary
                if (body instanceof FormData) {
                    return {body, headers: {}};
                }
                // Convert object to FormData
                const formData = new FormData();
                if (body && typeof body === 'object') {
                    Object.entries(body).forEach(([key, value]) => {
                        if (value instanceof File || value instanceof Blob) {
                            formData.append(key, value);
                        } else {
                            formData.append(key, String(value));
                        }
                    });
                }
                return {body: formData, headers: {}};

            case 'text':
                return {
                    body: String(body),
                    headers: {'Content-Type': CONTENT_TYPES.TEXT_PLAIN},
                };

            case 'json':
            default:
                return {
                    body: JSON.stringify(body),
                    headers: {'Content-Type': CONTENT_TYPES.JSON},
                };
        }
    }

    // ============================================
    // GET Methods (Overloaded)
    // ============================================

    /**
     * GET request - Basic
     */
    get<T = unknown>(endpoint: string): Promise<HttpResponse<T>>;

    /**
     * GET request - With options
     */
    get<T = unknown>(endpoint: string, options: HttpRequestOptions): Promise<HttpResponse<T>>;

    /**
     * GET request - Implementation
     */
    async get<T = unknown>(
        endpoint: string,
        options?: HttpRequestOptions
    ): Promise<HttpResponse<T>> {
        const fullEndpoint = this.buildEndpoint(endpoint, options?.params);

        return httpClient.fetch<T>(
            fullEndpoint,
            {method: HTTP_METHODS.GET},
            options
        );
    }

    // ============================================
    // POST Methods (Overloaded)
    // ============================================

    /**
     * POST request - No body
     */
    post<T = unknown>(endpoint: string): Promise<HttpResponse<T>>;

    /**
     * POST request - With body
     */
    post<T = unknown>(endpoint: string, body: unknown): Promise<HttpResponse<T>>;

    /**
     * POST request - With body and options
     */
    post<T = unknown>(
        endpoint: string,
        body: unknown,
        options: HttpBodyOptions
    ): Promise<HttpResponse<T>>;

    /**
     * POST request - Implementation
     */
    async post<T = unknown>(
        endpoint: string,
        body?: unknown,
        options?: HttpBodyOptions
    ): Promise<HttpResponse<T>> {
        const fullEndpoint = this.buildEndpoint(endpoint, options?.params);

        let requestBody: BodyInit | undefined;
        let bodyHeaders: Record<string, string> = {};

        if (body !== undefined) {
            const prepared = this.prepareBody(body, options?.contentType);
            requestBody = prepared.body;
            bodyHeaders = prepared.headers;
        }

        return httpClient.fetch<T>(
            fullEndpoint,
            {
                method: HTTP_METHODS.POST,
                body: requestBody,
            },
            {
                ...options,
                headers: {...bodyHeaders, ...options?.headers},
            }
        );
    }

    // ============================================
    // PUT Methods (Overloaded)
    // ============================================

    /**
     * PUT request - No body
     */
    put<T = unknown>(endpoint: string): Promise<HttpResponse<T>>;

    /**
     * PUT request - With body
     */
    put<T = unknown>(endpoint: string, body: unknown): Promise<HttpResponse<T>>;

    /**
     * PUT request - With body and options
     */
    put<T = unknown>(
        endpoint: string,
        body: unknown,
        options: HttpBodyOptions
    ): Promise<HttpResponse<T>>;

    /**
     * PUT request - Implementation
     */
    async put<T = unknown>(
        endpoint: string,
        body?: unknown,
        options?: HttpBodyOptions
    ): Promise<HttpResponse<T>> {
        const fullEndpoint = this.buildEndpoint(endpoint, options?.params);

        let requestBody: BodyInit | undefined;
        let bodyHeaders: Record<string, string> = {};

        if (body !== undefined) {
            const prepared = this.prepareBody(body, options?.contentType);
            requestBody = prepared.body;
            bodyHeaders = prepared.headers;
        }

        return httpClient.fetch<T>(
            fullEndpoint,
            {
                method: HTTP_METHODS.PUT,
                body: requestBody,
            },
            {
                ...options,
                headers: {...bodyHeaders, ...options?.headers},
            }
        );
    }

    // ============================================
    // PATCH Methods (Overloaded)
    // ============================================

    /**
     * PATCH request - No body
     */
    patch<T = unknown>(endpoint: string): Promise<HttpResponse<T>>;

    /**
     * PATCH request - With body
     */
    patch<T = unknown>(endpoint: string, body: unknown): Promise<HttpResponse<T>>;

    /**
     * PATCH request - With body and options
     */
    patch<T = unknown>(
        endpoint: string,
        body: unknown,
        options: HttpBodyOptions
    ): Promise<HttpResponse<T>>;

    /**
     * PATCH request - Implementation
     */
    async patch<T = unknown>(
        endpoint: string,
        body?: unknown,
        options?: HttpBodyOptions
    ): Promise<HttpResponse<T>> {
        const fullEndpoint = this.buildEndpoint(endpoint, options?.params);

        let requestBody: BodyInit | undefined;
        let bodyHeaders: Record<string, string> = {};

        if (body !== undefined) {
            const prepared = this.prepareBody(body, options?.contentType);
            requestBody = prepared.body;
            bodyHeaders = prepared.headers;
        }

        return httpClient.fetch<T>(
            fullEndpoint,
            {
                method: HTTP_METHODS.PATCH,
                body: requestBody,
            },
            {
                ...options,
                headers: {...bodyHeaders, ...options?.headers},
            }
        );
    }

    // ============================================
    // DELETE Methods (Overloaded)
    // ============================================

    /**
     * DELETE request - Basic
     */
    delete<T = unknown>(endpoint: string): Promise<HttpResponse<T>>;

    /**
     * DELETE request - With options
     */
    delete<T = unknown>(endpoint: string, options: HttpRequestOptions): Promise<HttpResponse<T>>;

    /**
     * DELETE request - With body and options
     */
    delete<T = unknown>(
        endpoint: string,
        body: unknown,
        options: HttpBodyOptions
    ): Promise<HttpResponse<T>>;

    /**
     * DELETE request - Implementation
     */
    async delete<T = unknown>(
        endpoint: string,
        bodyOrOptions?: unknown | HttpRequestOptions,
        options?: HttpBodyOptions
    ): Promise<HttpResponse<T>> {
        // Determine if second param is body or options
        let actualBody: unknown;
        let actualOptions: HttpBodyOptions | undefined;

        if (bodyOrOptions !== undefined) {
            if (
                options === undefined &&
                bodyOrOptions !== null &&
                typeof bodyOrOptions === 'object' &&
                ('params' in bodyOrOptions || 'headers' in bodyOrOptions || 'timeout' in bodyOrOptions)
            ) {
                // Second param is options
                actualOptions = bodyOrOptions as HttpBodyOptions;
            } else {
                // Second param is body
                actualBody = bodyOrOptions;
                actualOptions = options;
            }
        }

        const fullEndpoint = this.buildEndpoint(endpoint, actualOptions?.params);

        let requestBody: BodyInit | undefined;
        let bodyHeaders: Record<string, string> = {};

        if (actualBody !== undefined) {
            const prepared = this.prepareBody(actualBody, actualOptions?.contentType);
            requestBody = prepared.body;
            bodyHeaders = prepared.headers;
        }

        return httpClient.fetch<T>(
            fullEndpoint,
            {
                method: HTTP_METHODS.DELETE,
                body: requestBody,
            },
            {
                ...actualOptions,
                headers: {...bodyHeaders, ...actualOptions?.headers},
            }
        );
    }

    // ============================================
    // File Upload Helper
    // ============================================

    /**
     * Upload a file via POST
     */
    async uploadFile<T = unknown>(
        endpoint: string,
        file: File,
        fieldName: string = 'file',
        additionalData?: Record<string, string>,
        options?: HttpRequestOptions
    ): Promise<HttpResponse<T>> {
        const formData = new FormData();
        formData.append(fieldName, file);

        if (additionalData) {
            Object.entries(additionalData).forEach(([key, value]) => {
                formData.append(key, value);
            });
        }

        return this.post<T>(endpoint, formData, {
            ...options,
            contentType: 'form-data',
        });
    }

    // ============================================
    // Configuration Helpers
    // ============================================

    /**
     * Get current base URL
     */
    getBaseUrl(): string {
        return httpClient.getBaseUrl();
    }

    /**
     * Set base URL
     */
    setBaseUrl(url: string): void {
        httpClient.setBaseUrl(url);
    }

    /**
     * Set a default header
     */
    setDefaultHeader(key: string, value: string): void {
        httpClient.setDefaultHeader(key, value);
    }

    /**
     * Remove a default header
     */
    removeDefaultHeader(key: string): void {
        httpClient.removeDefaultHeader(key);
    }
}

// Export singleton instance
export const httpService = new HttpService();

// Export class for testing
export {HttpService};

// Re-export types and HttpError for convenience
export {HttpResponse, RequestConfig, HttpError};
