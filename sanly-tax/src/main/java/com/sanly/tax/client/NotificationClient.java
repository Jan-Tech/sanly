package com.sanly.tax.client;

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
    private final String       notificationUrl;
    private final String       serviceKey;

    public NotificationClient(RestTemplate restTemplate,
                               @Value("${notification.base-url:http://localhost:8088}") String notificationUrl,
                               @Value("${notification.service-key:tax-default-key-change-me}") String serviceKey) {
        this.restTemplate    = restTemplate;
        this.notificationUrl = notificationUrl;
        this.serviceKey      = serviceKey;
    }

    @Async
    public void send(String citizenNationalId, String eventType, String language,
                     Map<String, String> metadata) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Name", "SANLY_TAX");
            headers.set("X-Service-Key", serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    notificationUrl + "/api/v1/notifications/send",
                    new HttpEntity<>(Map.of(
                            "citizenNationalId", citizenNationalId,
                            "eventType", eventType,
                            "language", language,
                            "metadata", metadata != null ? metadata : Map.of()
                    ), headers),
                    Void.class);
        } catch (Exception ex) {
            log.warn("Notification dispatch failed [NIN={} event={}]: {}",
                    citizenNationalId, eventType, ex.getMessage());
        }
    }
}
