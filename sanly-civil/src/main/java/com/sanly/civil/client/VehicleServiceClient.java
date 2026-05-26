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
public class VehicleServiceClient {

    private final RestTemplate restTemplate;
    private final String vehicleBaseUrl;
    private final String lifeEventKey;

    public VehicleServiceClient(RestTemplate restTemplate,
                                 @Value("${life-event.vehicle-url:http://localhost:8096}") String vehicleBaseUrl,
                                 @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate = restTemplate;
        this.vehicleBaseUrl = vehicleBaseUrl;
        this.lifeEventKey = lifeEventKey;
    }

    @Async("lifeEventExecutor")
    public void handleOwnerDeceased(String deceasedNationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    vehicleBaseUrl + "/api/v1/vehicle/ownership/deceased",
                    new HttpEntity<>(Map.of("deceasedNationalId", deceasedNationalId), headers),
                    Void.class);
            log.info("[VEHICLE] Deceased ownership handling triggered for NIN={}", deceasedNationalId);
        } catch (Exception ex) {
            log.warn("[VEHICLE] Failed to handle deceased ownership for NIN={}: {}",
                    deceasedNationalId, ex.getMessage());
        }
    }
}
