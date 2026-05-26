package com.sanly.pension.client;

import com.sanly.pension.entity.PensionAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
    public void publishPensionStatus(PensionAccount account, String monthlyPensionAmount, int contributionMonths) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("citizenNationalId", account.getCitizenNationalId());
            payload.put("accountCode", account.getAccountCode());
            payload.put("status", account.getStatus().name());
            payload.put("eligibleAt", account.getEligibleAt().toString());
            payload.put("totalContributionMonths", contributionMonths);
            if (monthlyPensionAmount != null) payload.put("monthlyPensionAmount", monthlyPensionAmount);

            restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(Map.of(
                            "subjectNationalId", account.getCitizenNationalId(),
                            "dataType", "PENSION_STATUS",
                            "recordRef", account.getAccountCode(),
                            "payload", payload
                    ), headers), Void.class);
            log.info("[BRIDGE] Published PENSION_STATUS for account={}", account.getAccountCode());
        } catch (Exception ex) {
            log.warn("[BRIDGE] Failed to publish PENSION_STATUS for account={}: {}",
                    account.getAccountCode(), ex.getMessage());
        }
    }
}
