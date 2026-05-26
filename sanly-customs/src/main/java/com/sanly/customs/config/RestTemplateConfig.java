package com.sanly.customs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@Configuration
public class RestTemplateConfig {
    @Bean public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean("bridgePublishExecutor")
    public Executor bridgePublishExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4); ex.setMaxPoolSize(8);
        ex.setQueueCapacity(50); ex.setThreadNamePrefix("bridge-pub-");
        ex.initialize(); return ex;
    }
}
