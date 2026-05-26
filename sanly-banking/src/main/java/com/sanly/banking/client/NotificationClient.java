package com.sanly.banking.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notification.base-url}")  private String notificationBaseUrl;
    @Value("${notification.service-key}") private String serviceKey;

    /**
     * Sends an async notification to a citizen via the notification service.
     * Failures are logged but never propagated.
     */
    @Async("bankingExecutor")
    public void send(String recipientNationalId, String eventType, String language,
                     Map<String, String> templateParams) {
        try {
            String url = notificationBaseUrl + "/api/v1/notifications/send";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Key", serviceKey);

            Map<String, Object> body = new HashMap<>();
            body.put("recipientNationalId", recipientNationalId);
            body.put("eventType", eventType);
            body.put("language", language);
            body.put("templateParams", templateParams);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            log.debug("Notification sent: {} to {}", eventType, recipientNationalId);
        } catch (RestClientException e) {
            log.warn("NotificationClient.send failed [{}] for {}: {}", eventType, recipientNationalId, e.getMessage());
        }
    }
}
