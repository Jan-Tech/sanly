package com.sanly.registry.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
                               @Value("${notification.base-url:http://localhost:8088}") String baseUrl,
                               @Value("${notification.service-key:registry-default-key-change-me}") String serviceKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.serviceKey = serviceKey;
    }

    /** Sends the OTP immediately (not @Async) — must complete before returning sessionToken to client. */
    public void sendOtpSms(String nationalId, String phoneNumber, String otpCode, String language) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Name", "SANLY_REGISTRY");
            headers.set("X-Service-Key", serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(baseUrl + "/api/v1/notifications/send",
                    new HttpEntity<>(Map.of(
                            "citizenNationalId", nationalId,
                            "eventType", "OTP_LOGIN",
                            "language", language,
                            "metadata", Map.of(
                                    "otpCode", otpCode,
                                    "phoneNumber", phoneNumber
                            )), headers), Void.class);
            log.info("[NOTIFY] OTP_LOGIN SMS queued for NIN={}", nationalId);
        } catch (Exception ex) {
            log.warn("[NOTIFY] Could not send OTP notification for NIN={}: {}", nationalId, ex.getMessage());
        }
    }

    @Async
    public void send(String nationalId, String eventType, String language, Map<String, String> metadata) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Name", "SANLY_REGISTRY");
            headers.set("X-Service-Key", serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(baseUrl + "/api/v1/notifications/send",
                    new HttpEntity<>(Map.of("citizenNationalId", nationalId, "eventType", eventType,
                            "language", language, "metadata", metadata), headers), Void.class);
        } catch (Exception ex) {
            log.warn("[NOTIFY] Async send failed [{}/{}]: {}", nationalId, eventType, ex.getMessage());
        }
    }
}
