package com.rickm.filecompression.config;

import com.rickm.filecompression.properties.CorsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration for development and production.
 * Allows cross-origin requests from the frontend dev server.
 */
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Autowired
    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Use values from properties
        corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);

        // Set allowed methods (with defaults if empty)
        if (corsProperties.getAllowedMethods().isEmpty()) {
            config.addAllowedMethod("*");
        } else {
            corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);
        }

        // Set allowed headers (with defaults if empty)
        if (corsProperties.getAllowedHeaders().isEmpty()) {
            config.addAllowedHeader("*");
        } else {
            corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);
        }

        config.setAllowCredentials(corsProperties.isAllowCredentials());

        config.setExposedHeaders(List.of("X-Operation-Id", "X-Original-Size", "X-Compressed-Size", "X-Compression-Ratio", "X-Processing-Time"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
