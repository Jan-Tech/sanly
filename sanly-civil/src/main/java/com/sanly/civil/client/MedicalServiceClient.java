package com.sanly.civil.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

/**
 * Calls sanly-medical to create a vaccination schedule for a newborn.
 */
@Slf4j
@Component
public class MedicalServiceClient {

    private final RestTemplate restTemplate;
    private final String       medicalBaseUrl;
    private final String       lifeEventKey;

    public MedicalServiceClient(RestTemplate restTemplate,
                                 @Value("${life-event.medical-url:http://localhost:8082}") String medicalBaseUrl,
                                 @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate  = restTemplate;
        this.medicalBaseUrl = medicalBaseUrl;
        this.lifeEventKey   = lifeEventKey;
    }

    public void scheduleVaccinations(String childNin, LocalDate birthDate,
                                      String motherNin, String fatherNin) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    medicalBaseUrl + "/api/v1/vaccinations/schedule",
                    new HttpEntity<>(Map.of(
                            "citizenNationalId", childNin,
                            "birthDate",         birthDate.toString(),
                            "motherNationalId",  motherNin != null ? motherNin : "",
                            "fatherNationalId",  fatherNin != null ? fatherNin : ""
                    ), headers),
                    Void.class);
            log.info("Vaccination schedule triggered for child NIN={}", childNin);
        } catch (Exception ex) {
            log.warn("Failed to schedule vaccinations for NIN={}: {}", childNin, ex.getMessage());
        }
    }
}
