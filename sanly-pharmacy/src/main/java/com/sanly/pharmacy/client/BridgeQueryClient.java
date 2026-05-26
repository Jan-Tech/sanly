package com.sanly.pharmacy.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Queries SANLY Bridge for PRESCRIPTION records published by INST_MEDICAL.
 * Used to verify prescription status before dispensing.
 */
@Slf4j
@Component
public class BridgeQueryClient {

    private final RestTemplate restTemplate;
    private final String       bridgeBaseUrl;
    private final String       institutionCode;
    private final String       institutionKey;

    public BridgeQueryClient(RestTemplate restTemplate,
                              @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                              @Value("${sanly.bridge.institution-code}") String institutionCode,
                              @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate    = restTemplate;
        this.bridgeBaseUrl   = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey  = institutionKey;
    }

    /**
     * Queries active PRESCRIPTION records from INST_MEDICAL for a citizen.
     * Returns list of prescription summaries (Map) or empty list on any error.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryPrescriptions(String nationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key",  institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "nationalId",   nationalId,
                    "dataType",     "PRESCRIPTION",
                    "targetCode",   "INST_MEDICAL",
                    "purposeCode",  "MEDICAL_CLEARANCE",
                    "caseReference", "PHARMACY_DISPENSE"
            );

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    bridgeBaseUrl + "/api/v1/exchange/query",
                    new HttpEntity<>(body, headers), Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object records = resp.getBody().get("records");
                if (records instanceof List<?> list) {
                    return (List<Map<String, Object>>) list;
                }
            }
        } catch (Exception ex) {
            log.warn("Bridge query failed for NIN={}: {}", nationalId, ex.getMessage());
        }
        return List.of();
    }
}
