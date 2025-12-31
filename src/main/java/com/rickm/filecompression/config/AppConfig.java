package com.rickm.filecompression.config;

import com.rickm.filecompression.properties.AsyncProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Application configuration for async processing.
 */
@Configuration
public class AppConfig {

    private final AsyncProperties asyncProperties;

    @Autowired
    public AppConfig(AsyncProperties asyncProperties) {
        this.asyncProperties = asyncProperties;
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix(asyncProperties.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }

}
