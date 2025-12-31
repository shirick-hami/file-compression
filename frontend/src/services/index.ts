// Export all services

// HTTP Client (Global entry point)
export {httpClient, HttpClient, HttpError} from './http-client';
export type {RequestConfig, HttpResponse} from './http-client';

// HTTP Service (High-level API)
export {httpService, HttpService} from './http.service';
export type {HttpRequestOptions, HttpBodyOptions} from './http.service';

// API Service (Application-specific)
export {apiService, ApiService} from './api.service';
export type {
    CompressionResult,
    DecompressionResult,
    AnalysisResult,
    ValidationResult,
    HealthResult
} from './api.service';
