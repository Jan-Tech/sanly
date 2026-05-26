package com.sanly.civil.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Calls sanly-business internal life-event endpoint to suspend businesses of deceased owner.
 */
@Slf4j
@Component
public class BusinessServiceClient {

    private final RestTemplate restTemplate;
    private final String       businessBaseUrl;
    private final String       lifeEventKey;

    public BusinessServiceClient(RestTemplate restTemplate,
                                  @Value("${life-event.business-url:http://localhost:8086}") String businessBaseUrl,
                                  @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate    = restTemplate;
        this.businessBaseUrl = businessBaseUrl;
        this.lifeEventKey    = lifeEventKey;
    }

    public void handleOwnerDeceased(String nationalId, String deceasedFullName, String dateOfDeath) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    businessBaseUrl + "/api/v1/businesses/owner-deceased",
                    new HttpEntity<>(Map.of(
                            "nationalId",       nationalId,
                            "deceasedFullName", deceasedFullName != null ? deceasedFullName : "",
                            "dateOfDeath",      dateOfDeath != null ? dateOfDeath : ""
                    ), headers),
                    Void.class);
            log.info("Business suspension triggered for deceased NIN={}", nationalId);
        } catch (Exception ex) {
            log.warn("Failed to suspend businesses for deceased NIN={}: {}", nationalId, ex.getMessage());
        }
    }
}
