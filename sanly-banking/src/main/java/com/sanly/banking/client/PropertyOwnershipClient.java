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
public class PropertyOwnershipClient {

    private final RestTemplate restTemplate;

    @Value("${services.land-url}")   private String landUrl;
    @Value("${services.land-token}") private String landToken;

    /**
     * Fetches property ownership summary.
     * - 0 properties → count: 0, totalValueRange: null
     * - 1+ properties → count + value range (LOW_VALUE / MEDIUM_VALUE / HIGH_VALUE)
     * Returns null on error (UNAVAILABLE).
     */
    public PropertyInfo getPropertyInfo(String nationalId) {
        try {
            String url = landUrl + "/api/v1/properties?ownerNationalId=" + nationalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(landToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<PropertyInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, PropertyInfo.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("PropertyOwnershipClient.getPropertyInfo failed for {}: {}", nationalId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class PropertyInfo {
        private int count;
        private String totalValueRange; // LOW_VALUE, MEDIUM_VALUE, HIGH_VALUE, or null
    }
}
