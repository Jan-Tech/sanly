package com.sanly.pension.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@Configuration
public class RestTemplateConfig {
    @Bean public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean(name = "bridgePublishExecutor")
    public Executor bridgePublishExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2); ex.setMaxPoolSize(5); ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("bridge-publish-"); ex.initialize(); return ex;
    }
}
