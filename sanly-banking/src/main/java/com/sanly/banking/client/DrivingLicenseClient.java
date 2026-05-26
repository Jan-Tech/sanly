package com.sanly.banking.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrivingLicenseClient {

    private final RestTemplate restTemplate;

    @Value("${services.dmv-url}")   private String dmvUrl;
    @Value("${services.dmv-token}") private String dmvToken;

    /**
     * Fetches driving license info. Returns null on error.
     */
    public LicenseInfo getLicenseInfo(String nationalId) {
        try {
            String url = dmvUrl + "/api/v1/dmv/licenses/" + nationalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(dmvToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<LicenseInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, LicenseInfo.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("DrivingLicenseClient.getLicenseInfo failed for {}: {}", nationalId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class LicenseInfo {
        private String status;        // ACTIVE, EXPIRED, SUSPENDED, NONE
        private List<String> categories; // e.g. ["B", "C"]
        private String expiryDate;
    }
}
