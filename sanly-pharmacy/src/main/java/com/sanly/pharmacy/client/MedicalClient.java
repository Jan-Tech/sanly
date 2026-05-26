package com.sanly.pharmacy.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Calls sanly-medical to record dispensing.
 * Uses the pharmacy's raw API key (X-Pharmacy-Code + X-Pharmacy-Key headers).
 */
@Slf4j
@Component
public class MedicalClient {

    private final RestTemplate restTemplate;
    private final String       medicalBaseUrl;
    private final String       pharmacyCode;
    private final String       pharmacyKey;

    public MedicalClient(RestTemplate restTemplate,
                          @Value("${sanly.medical.base-url}") String medicalBaseUrl,
                          @Value("${sanly.medical.pharmacy-code}") String pharmacyCode,
                          @Value("${sanly.medical.pharmacy-key}") String pharmacyKey) {
        this.restTemplate   = restTemplate;
        this.medicalBaseUrl = medicalBaseUrl;
        this.pharmacyCode   = pharmacyCode;
        this.pharmacyKey    = pharmacyKey;
    }

    /**
     * Records dispensing on sanly-medical and returns the updated prescription status.
     *
     * @throws IllegalStateException if medical service rejects the request (already dispensed, expired, etc.)
     */
    public Map<?, ?> dispense(String prescriptionCode, int quantityDispensed,
                               String pharmacistNin, String notes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Pharmacy-Code", pharmacyCode);
            headers.set("X-Pharmacy-Key",  pharmacyKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "quantityDispensed",    quantityDispensed,
                    "pharmacistNationalId", pharmacistNin != null ? pharmacistNin : "",
                    "notes",                notes != null ? notes : ""
            );

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    medicalBaseUrl + "/api/v1/prescriptions/" + prescriptionCode + "/dispense",
                    new HttpEntity<>(body, headers), Map.class);

            return resp.getBody();

        } catch (HttpClientErrorException ex) {
            String msg = ex.getResponseBodyAsString();
            throw new IllegalStateException("Medical service rejected dispense: " + msg, ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Medical service unreachable: " + ex.getMessage(), ex);
        }
    }
}
