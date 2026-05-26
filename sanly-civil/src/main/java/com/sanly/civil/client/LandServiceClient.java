package com.sanly.civil.client;

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
public class LandServiceClient {

    private final RestTemplate restTemplate;
    private final String landBaseUrl;
    private final String lifeEventKey;

    public LandServiceClient(RestTemplate restTemplate,
                              @Value("${life-event.land-url:http://localhost:8091}") String landBaseUrl,
                              @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate = restTemplate;
        this.landBaseUrl = landBaseUrl;
        this.lifeEventKey = lifeEventKey;
    }

    @Async("lifeEventExecutor")
    public void handleOwnerDeceased(String deceasedNationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    landBaseUrl + "/api/v1/land/ownership/deceased",
                    new HttpEntity<>(Map.of("deceasedNationalId", deceasedNationalId), headers),
                    Void.class);
            log.info("[LAND] Deceased ownership handling triggered for NIN={}", deceasedNationalId);
        } catch (Exception ex) {
            log.warn("[LAND] Failed to handle deceased ownership for NIN={}: {}",
                    deceasedNationalId, ex.getMessage());
        }
    }
}
