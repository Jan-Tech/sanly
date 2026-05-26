package com.sanly.appointments.client;

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

    @Value("${notification.service-key:appointments-default-key-change-me}")
    private String serviceKey;

    @Async("appointmentsExecutor")
    public void send(String nationalId, String eventType, String language, Map<String, String> metadata) {
        try {
            String url = baseUrl + "/api/v1/notifications/send";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Name", "SANLY_APPOINTMENTS");
            headers.set("X-Service-Key", serviceKey);

            Map<String, Object> body = new HashMap<>();
            body.put("nationalId", nationalId);
            body.put("eventType", eventType);
            body.put("language", language != null ? language : "tk");
            body.put("metadata", metadata != null ? metadata : Map.of());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForObject(url, entity, Map.class);

            log.debug("Notification sent: nationalId={}, eventType={}", nationalId, eventType);
        } catch (Exception e) {
            log.warn("Failed to send notification: nationalId={}, eventType={}, error={}",
                    nationalId, eventType, e.getMessage());
        }
    }
}
