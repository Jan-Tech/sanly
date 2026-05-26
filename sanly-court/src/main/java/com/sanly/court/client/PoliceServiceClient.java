package com.sanly.court.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Calls sanly-police to auto-register a criminal conviction after a GUILTY verdict.
 * Requires sanly-police to accept X-Court-Service-Key for court-initiated criminal records.
 * If police service is unavailable or rejects the call, the error is logged and ignored —
 * the COURT_ORDER published to Bridge is the authoritative integration.
 */
@Slf4j
@Component
public class PoliceServiceClient {
    private final RestTemplate restTemplate;
    private final String policeBaseUrl;
    private final String serviceKey;

    public PoliceServiceClient(RestTemplate restTemplate,
                                @Value("${sanly.police.base-url:http://localhost:8084}") String policeBaseUrl,
                                @Value("${sanly.police.service-key:court-police-key-change-me}") String serviceKey) {
        this.restTemplate = restTemplate; this.policeBaseUrl = policeBaseUrl; this.serviceKey = serviceKey;
    }

    @Async("bridgePublishExecutor")
    public void addCriminalRecord(String nationalId, String caseNumber, String verdictSummary) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Court-Service-Key", serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    policeBaseUrl + "/api/v1/criminal-records/court-conviction",
                    new HttpEntity<>(Map.of(
                            "nationalId", nationalId,
                            "caseNumber", caseNumber,
                            "verdict", "GUILTY",
                            "summary", verdictSummary != null ? verdictSummary : "Court conviction"
                    ), headers), Void.class);
            log.info("[POLICE] Criminal record added for {} case {}", nationalId, caseNumber);
        } catch (Exception ex) {
            log.warn("[POLICE] Failed to add criminal record for {} (non-fatal, Bridge has COURT_ORDER): {}",
                    nationalId, ex.getMessage());
        }
    }
}
