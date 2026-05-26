package com.sanly.banking.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PensionStatusClient {

    private final RestTemplate restTemplate;

    @Value("${services.pension-url}")   private String pensionUrl;
    @Value("${services.pension-token}") private String pensionToken;

    /**
     * Fetches pension account status. Returns null on error.
     */
    public PensionInfo getPensionInfo(String nationalId) {
        try {
            String url = pensionUrl + "/api/v1/pension/account/" + nationalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(pensionToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<PensionInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, PensionInfo.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("PensionStatusClient.getPensionInfo failed for {}: {}", nationalId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class PensionInfo {
        private String accountStatus;       // ACTIVE, INACTIVE, NOT_REGISTERED
        private String estimatedMonthly;    // LOW / MEDIUM / HIGH range
        private boolean activeContributor;  // employment indicator
    }
}
