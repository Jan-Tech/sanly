package com.sanly.tax.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publishes TAX_STATUS to SANLY Bridge asynchronously.
 * Triggered on taxpayer registration and on filing acceptance/rejection.
 * Never throws — logs errors only.
 */
@Slf4j @Service @RequiredArgsConstructor
public class BridgePublisherService {

    private final RestTemplate restTemplate;
    @Value("${sanly.bridge.base-url}") private String baseUrl;
    @Value("${sanly.bridge.institution-code}") private String institutionCode;
    @Value("${sanly.bridge.institution-key}") private String institutionKey;

    @Async("bridgePublishExecutor")
    public void publishTaxStatus(String citizenNationalId, String taxId,
                                  String taxpayerType, String complianceStatus,
                                  Integer lastFilingYear) {
        try {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("complianceStatus", complianceStatus);
            summary.put("taxId", taxId);
            summary.put("taxpayerType", taxpayerType);
            summary.put("lastFilingYear", lastFilingYear);
            summary.put("updatedAt", LocalDateTime.now().toString());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("nationalId", citizenNationalId);
            body.put("dataType", "TAX_STATUS");
            body.put("recordRef", taxId);
            body.put("summary", summary);
            body.put("expiresAt", null);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.postForEntity(baseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Object.class);

            log.info("Published TAX_STATUS for NIN={} complianceStatus={}", citizenNationalId, complianceStatus);
        } catch (Exception e) {
            log.error("Failed to publish TAX_STATUS for {}: {}", citizenNationalId, e.getMessage());
        }
    }
}
