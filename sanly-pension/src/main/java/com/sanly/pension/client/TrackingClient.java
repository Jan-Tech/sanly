package com.sanly.pension.client;

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
public class TrackingClient {

    private final RestTemplate restTemplate;

    @Value("${tracking.base-url:http://localhost:8102}")
    private String baseUrl;

    @Value("${tracking.service-key:tracking-service-key-change-me}")
    private String serviceKey;

    @Async
    public void push(String nationalId, String itemType, String sourceService,
                     String sourceItemCode, String title, String currentStatus,
                     String statusDescription, boolean isCompleted) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Key", serviceKey);

            Map<String, Object> body = new HashMap<>();
            body.put("citizenNationalId", nationalId);
            body.put("itemType", itemType);
            body.put("sourceService", sourceService);
            body.put("sourceItemCode", sourceItemCode);
            body.put("title", title);
            body.put("currentStatus", currentStatus);
            body.put("isCompleted", isCompleted);
            if (statusDescription != null && !statusDescription.isBlank()) {
                body.put("statusDescription", statusDescription);
            }

            restTemplate.postForObject(baseUrl + "/api/v1/documents/tracking/update",
                    new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) {
            log.warn("Failed to push tracking update: source={}, status={}, error={}",
                    sourceItemCode, currentStatus, e.getMessage());
        }
    }
}
