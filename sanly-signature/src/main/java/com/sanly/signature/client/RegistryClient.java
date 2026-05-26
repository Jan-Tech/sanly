package com.sanly.signature.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RegistryClient {

    private static final Logger log = LoggerFactory.getLogger(RegistryClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String serviceName;
    private final String serviceKey;

    public RegistryClient(RestTemplate restTemplate,
                          @Value("${registry.base-url}") String baseUrl,
                          @Value("${registry.service-name}") String serviceName,
                          @Value("${registry.service-key}") String serviceKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.serviceName = serviceName;
        this.serviceKey = serviceKey;
    }

    /**
     * Calls citizen-registry to verify an action OTP.
     *
     * @return true if the OTP is valid, false on any failure or invalid response
     */
    public boolean verifyActionOtp(String nationalId, String otpCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Name", serviceName);
            headers.set("X-Service-Key", serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of("nationalId", nationalId, "otpCode", otpCode);
            var response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/auth/verify-action-otp",
                    new HttpEntity<>(body, headers),
                    Map.class);

            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    Object valid = dataMap.get("valid");
                    return Boolean.TRUE.equals(valid);
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("OTP verification call to registry failed for NIN={}: {}", nationalId, e.getMessage());
            return false;
        }
    }

    /**
     * Fetches the citizen's full name from the registry.
     *
     * @param nationalId   citizen national ID
     * @param bearerToken  the citizen's own JWT (forwarded from request)
     * @return "firstName lastName", or null if unavailable
     */
    public String getCitizenName(String nationalId, String bearerToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken);

            var response = restTemplate.exchange(
                    baseUrl + "/api/v1/citizens/" + nationalId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class);

            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    String firstName = (String) dataMap.get("firstName");
                    String lastName  = (String) dataMap.get("lastName");
                    if (firstName != null && lastName != null) {
                        return firstName + " " + lastName;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to fetch citizen name for NIN={}: {}", nationalId, e.getMessage());
            return null;
        }
    }
}
