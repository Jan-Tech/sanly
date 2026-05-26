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
 * Calls sanly-tax internal life-event endpoints.
 * Uses X-Life-Event-Key for authentication.
 */
@Slf4j
@Component
public class TaxServiceClient {

    private final RestTemplate restTemplate;
    private final String       taxBaseUrl;
    private final String       lifeEventKey;

    public TaxServiceClient(RestTemplate restTemplate,
                             @Value("${life-event.tax-url:http://localhost:8085}") String taxBaseUrl,
                             @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate = restTemplate;
        this.taxBaseUrl   = taxBaseUrl;
        this.lifeEventKey = lifeEventKey;
    }

    public void triggerChildBenefit(String childNin, String motherNin, String fatherNin,
                                     LocalDate birthDate) {
        try {
            post("/api/v1/benefits/child-benefit", Map.of(
                    "childNationalId",  childNin,
                    "motherNationalId", motherNin != null ? motherNin : "",
                    "fatherNationalId", fatherNin != null ? fatherNin : "",
                    "birthDate",        birthDate.toString()
            ));
            log.info("Child benefit triggered for child NIN={}", childNin);
        } catch (Exception ex) {
            log.warn("Failed to trigger child benefit for NIN={}: {}", childNin, ex.getMessage());
        }
    }

    public void cancelBenefits(String nationalId) {
        try {
            post("/api/v1/benefits/cancel", Map.of("nationalId", nationalId));
            log.info("Benefits cancelled for NIN={}", nationalId);
        } catch (Exception ex) {
            log.warn("Failed to cancel benefits for NIN={}: {}", nationalId, ex.getMessage());
        }
    }

    public void deregisterTaxpayer(String nationalId) {
        try {
            post("/api/v1/taxpayers/deregister", Map.of("nationalId", nationalId));
            log.info("Taxpayer deregistered for NIN={}", nationalId);
        } catch (Exception ex) {
            log.warn("Failed to deregister taxpayer NIN={}: {}", nationalId, ex.getMessage());
        }
    }

    public void updateMaritalStatus(String nationalId, String maritalStatus, String spouseNin) {
        try {
            post("/api/v1/taxpayers/update-marital-status", Map.of(
                    "nationalId",      nationalId,
                    "maritalStatus",   maritalStatus,
                    "spouseNationalId", spouseNin != null ? spouseNin : ""
            ));
            log.info("Marital status updated to {} for NIN={}", maritalStatus, nationalId);
        } catch (Exception ex) {
            log.warn("Failed to update marital status for NIN={}: {}", nationalId, ex.getMessage());
        }
    }

    private void post(String path, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Life-Event-Key", lifeEventKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(taxBaseUrl + path, new HttpEntity<>(body, headers), Void.class);
    }
}
