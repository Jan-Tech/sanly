package com.sanly.analytics.client;

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
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notification.base-url:http://localhost:8088}")
    private String baseUrl;

    @Value("${notification.service-key:analytics-default-key-change-me}")
    private String serviceKey;

    @Async("analyticsExecutor")
    public void send(String nationalId, String eventType, String language, Map<String, String> metadata) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Name", "SANLY_ANALYTICS");
            headers.set("X-Service-Key", serviceKey);

            Map<String, Object> body = new HashMap<>();
            body.put("nationalId", nationalId);
            body.put("eventType", eventType);
            body.put("language", language != null ? language : "EN");
            body.put("metadata", metadata != null ? metadata : Map.of());

            restTemplate.postForObject(baseUrl + "/api/v1/notifications/send",
                    new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) {
            log.warn("Failed to send notification: eventType={}, error={}", eventType, e.getMessage());
        }
    }
}
