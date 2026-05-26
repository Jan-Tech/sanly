package com.sanly.banking.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistryClient {

    private final RestTemplate restTemplate;

    @Value("${registry.base-url}")    private String baseUrl;
    @Value("${registry.service-key}") private String serviceKey;

    /**
     * Verifies an action OTP for a citizen.
     * Calls POST {REGISTRY_URL}/api/v1/auth/verify-action-otp
     * with X-Service-Key header.
     *
     * @return true if verified; false if invalid OTP; null if service unavailable
     */
    public Boolean verifyActionOtp(String nationalId, String otpCode) {
        try {
            String url = baseUrl + "/api/v1/auth/verify-action-otp";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Service-Key", serviceKey);

            Map<String, String> body = Map.of(
                    "nationalId", nationalId,
                    "otpCode", otpCode,
                    "purpose", "BANKING_CONSENT"
            );

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Boolean.class);

            return Boolean.TRUE.equals(response.getBody());
        } catch (RestClientException e) {
            log.error("RegistryClient.verifyActionOtp unavailable for {}: {}", nationalId, e.getMessage());
            return null; // null = service unavailable
        }
    }
}
