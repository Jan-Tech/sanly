package com.sanly.social.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class NotificationClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String serviceKey;

    public NotificationClient(RestTemplate restTemplate,
                               @Value("${notification.base-url}") String baseUrl,
                               @Value("${notification.service-key}") String serviceKey) {
        this.restTemplate = restTemplate; this.baseUrl = baseUrl; this.serviceKey = serviceKey;
    }

    @Async
    public void send(String nationalId, String eventType, String language, Map<String, String> metadata) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Name", "SANLY_SOCIAL");
            headers.set("X-Service-Key", serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(baseUrl + "/api/v1/notifications/send",
                    new HttpEntity<>(Map.of("citizenNationalId", nationalId, "eventType", eventType,
                            "language", language, "metadata", metadata), headers), Void.class);
        } catch (Exception ex) {
            log.warn("Notification send failed [{}/{}]: {}", nationalId, eventType, ex.getMessage());
        }
    }
}
