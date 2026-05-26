package com.sanly.banking.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessOwnershipClient {

    private final RestTemplate restTemplate;

    @Value("${services.business-url}")   private String businessUrl;
    @Value("${services.business-token}") private String businessToken;

    /**
     * Fetches businesses owned by the citizen.
     * Returns empty list on error (never propagates exception).
     */
    public List<BusinessInfo> getOwnedBusinesses(String nationalId) {
        try {
            String url = businessUrl + "/api/v1/businesses?ownerNationalId=" + nationalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(businessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<List<BusinessInfo>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<BusinessInfo>>() {});
            List<BusinessInfo> body = response.getBody();
            return body != null ? body : Collections.emptyList();
        } catch (RestClientException e) {
            log.warn("BusinessOwnershipClient.getOwnedBusinesses failed for {}: {}", nationalId, e.getMessage());
            return null; // null = UNAVAILABLE
        }
    }

    @Data
    public static class BusinessInfo {
        private String name;
        private String registrationNumber;
        private String status;
    }
}
