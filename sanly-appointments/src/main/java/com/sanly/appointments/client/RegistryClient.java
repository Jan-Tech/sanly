package com.sanly.appointments.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistryClient {

    private final RestTemplate restTemplate;

    @Value("${registry.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${registry.institution-token}")
    private String institutionToken;

    /**
     * Verifies that a citizen is active in the civil registry.
     * Fail-open: returns true on any exception (prototype behavior).
     */
    public boolean verifyCitizen(String nationalId) {
        try {
            String url = baseUrl + "/api/v1/citizens/" + nationalId + "/verify";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(institutionToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    Object active = dataMap.get("active");
                    return Boolean.TRUE.equals(active);
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("Registry verification failed for nationalId={}, failing open: {}", nationalId, e.getMessage());
            return true;
        }
    }
}
