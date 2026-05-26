package com.sanly.court.client;

import com.sanly.court.entity.CourtCase;
import com.sanly.court.entity.Verdict;
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

@Slf4j
@Component
public class BridgePublisherService {
    private final RestTemplate restTemplate;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${sanly.bridge.institution-code}") String institutionCode,
                                   @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate; this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode; this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishCourtOrder(CourtCase courtCase, Verdict verdict, String fineAmount) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("caseNumber", courtCase.getCaseNumber());
            payload.put("caseType", courtCase.getCaseType().name());
            payload.put("defendantNationalId", courtCase.getDefendantNationalId());
            payload.put("verdictType", verdict.getVerdictType().name());
            payload.put("issuedAt", verdict.getIssuedAt().toString());
            payload.put("appealDeadline", verdict.getAppealDeadline().toString());
            if (fineAmount != null) payload.put("fineAmount", fineAmount);

            Map<String, Object> body = Map.of(
                    "subjectNationalId", courtCase.getDefendantNationalId(),
                    "dataType", "COURT_ORDER",
                    "recordRef", courtCase.getCaseNumber(),
                    "payload", payload
            );

            restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Void.class);
            log.info("[BRIDGE] Published COURT_ORDER for case {}", courtCase.getCaseNumber());
        } catch (Exception ex) {
            log.warn("[BRIDGE] Failed to publish COURT_ORDER for case {}: {}",
                    courtCase.getCaseNumber(), ex.getMessage());
        }
    }
}
