package com.sanly.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsClient {

    private final RestTemplate restTemplate;

    @Value("${analytics.base-url:http://localhost:8103}")
    private String baseUrl;

    @Value("${analytics.service-key:analytics-ingest-key-change-me}")
    private String serviceKey;

    @Async
    public void pushDaily(Map<String, Object> data) {
        push("/api/v1/analytics/ingest/daily", data);
    }

    @Async
    public void pushServiceUsage(String endpoint, long requestCount, double avgResponseMs, long errorCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("serviceName", "CITIZEN_REGISTRY");
        data.put("endpoint", endpoint);
        data.put("requestCount", requestCount);
        data.put("avgResponseMs", avgResponseMs);
        data.put("errorCount", errorCount);
        push("/api/v1/analytics/ingest/service-usage", data);
    }

    private void push(String path, Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Key", serviceKey);
            restTemplate.postForObject(baseUrl + path, new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) {
            log.warn("Analytics push failed: path={}, error={}", path, e.getMessage());
        }
    }
}
