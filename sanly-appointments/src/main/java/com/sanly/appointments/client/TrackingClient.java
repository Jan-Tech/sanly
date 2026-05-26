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

/**
 * Pushes appointment status updates to sanly-documents Status Tracker.
 * itemType: APPOINTMENT
 * sourceService: APPOINTMENTS (not in SourceService enum — ignored by tracker)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingClient {

    private final RestTemplate restTemplate;

    @Value("${tracking.base-url:http://localhost:8102}")
    private String baseUrl;

    @Value("${tracking.service-key:tracking-service-key-change-me}")
    private String serviceKey;

    /**
     * @param nationalId         citizen's NIN
     * @param itemType           e.g. "APPOINTMENT"
     * @param sourceService      e.g. "DMV" — SourceService enum value in sanly-documents
     * @param sourceItemCode     e.g. "TM-APT-2026000001"
     * @param title              human-readable title shown in tracker UI
     * @param currentStatus      e.g. "BOOKED", "COMPLETED", "CANCELLED"
     * @param statusDescription  optional detail shown in timeline
     * @param isCompleted        true when terminal status (COMPLETED, CANCELLED, NO_SHOW)
     */
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
