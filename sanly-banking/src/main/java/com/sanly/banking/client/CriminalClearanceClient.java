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
public class CriminalClearanceClient {

    private final RestTemplate restTemplate;

    @Value("${services.police-url}")   private String policeUrl;
    @Value("${services.police-token}") private String policeToken;

    /**
     * Fetches criminal clearance status.
     * Returns "CLEAR" if no records, "HAS_RECORD" if any exist, null on error.
     * Never returns the actual record details — binary yes/no only.
     */
    public ClearanceInfo getClearance(String nationalId) {
        try {
            String url = policeUrl + "/api/v1/police/records/" + nationalId + "/clearance";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(policeToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<ClearanceInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, ClearanceInfo.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("CriminalClearanceClient.getClearance failed for {}: {}", nationalId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class ClearanceInfo {
        private String status; // CLEAR or HAS_RECORD
    }
}
