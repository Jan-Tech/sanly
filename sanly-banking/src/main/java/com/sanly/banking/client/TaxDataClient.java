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
public class TaxDataClient {

    private final RestTemplate restTemplate;

    @Value("${services.tax-url}")   private String taxUrl;
    @Value("${services.tax-token}") private String taxToken;

    /**
     * Fetches tax info for citizen. Returns null if call fails.
     */
    public TaxInfo getTaxInfo(String nationalId) {
        try {
            String url = taxUrl + "/api/v1/tax/citizens/" + nationalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(taxToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<TaxInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, TaxInfo.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("TaxDataClient.getTaxInfo failed for {}: {}", nationalId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class TaxInfo {
        private String taxId;
        private String complianceStatus;  // COMPLIANT, NON_COMPLIANT
        private Integer lastFilingYear;
        private String incomeClass;       // LOW, MEDIUM, HIGH, UNKNOWN
    }
}
