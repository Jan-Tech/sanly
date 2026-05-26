package com.sanly.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Executor for async SANLY Bridge publishing.
     * DelegatingSecurityContextExecutor propagates the Spring Security context
     * to the worker thread so the authenticated doctor is visible in async logs.
     */
    @Bean(name = "bridgePublishExecutor")
    public Executor bridgePublishExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("bridge-pub-");
        executor.initialize();
        return new DelegatingSecurityContextExecutor(executor);
    }
}
