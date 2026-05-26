package com.sanly.tax.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;

@Configuration @EnableAsync
public class AsyncConfig {
    @Bean(name = "bridgePublishExecutor")
    public Executor bridgePublishExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2); ex.setMaxPoolSize(4); ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("tax-bridge-"); ex.initialize();
        return new DelegatingSecurityContextExecutor(ex);
    }
}
