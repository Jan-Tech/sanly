package com.sanly.social.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class RestTemplateConfig {
    @Bean public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean(name = "bridgePublishExecutor")
    public Executor bridgePublishExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2); exec.setMaxPoolSize(5);
        exec.setThreadNamePrefix("bridge-pub-"); exec.initialize();
        return exec;
    }
}
