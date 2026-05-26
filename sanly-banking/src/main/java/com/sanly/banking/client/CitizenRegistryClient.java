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
public class CitizenRegistryClient {

    private final RestTemplate restTemplate;

    @Value("${registry.base-url}") private String baseUrl;
    @Value("${registry.institution-token}") private String institutionToken;

    /**
     * Fetches basic citizen info. Returns null if call fails (never propagates exception).
     */
    public CitizenInfo getCitizenBasic(String nationalId) {
        try {
            String url = baseUrl + "/api/v1/citizens/" + nationalId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(institutionToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<CitizenInfo> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CitizenInfo.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("CitizenRegistryClient.getCitizenBasic failed for {}: {}", nationalId, e.getMessage());
            return null;
        }
    }

    /**
     * Checks whether a citizen exists by national ID.
     */
    public boolean citizenExists(String nationalId) {
        return getCitizenBasic(nationalId) != null;
    }

    @Data
    public static class CitizenInfo {
        private String nationalId;    // masked
        private String firstName;
        private String lastName;
        private String dateOfBirth;
        private String gender;
        private String address;
        private String maskedPhone;
    }
}
