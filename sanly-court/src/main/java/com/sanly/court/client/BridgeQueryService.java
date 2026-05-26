package com.sanly.court.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BridgeQueryService {
    private final RestTemplate restTemplate;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgeQueryService(RestTemplate restTemplate,
                               @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                               @Value("${sanly.bridge.institution-code}") String institutionCode,
                               @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate; this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode; this.institutionKey = institutionKey;
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Institution-Code", institutionCode);
        h.set("X-Institution-Key", institutionKey);
        return h;
    }

    public boolean hasCriminalRecord(String nationalId) {
        try {
            var response = restTemplate.exchange(
                    bridgeBaseUrl + "/api/v1/exchange/data/" + nationalId + "/CRIMINAL_RECORD",
                    HttpMethod.GET, new HttpEntity<>(headers()),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody() != null && !response.getBody().isEmpty();
        } catch (Exception ex) {
            log.warn("Bridge CRIMINAL_RECORD query failed for {}: {}", nationalId, ex.getMessage());
            return false;
        }
    }

    public boolean hasPropertyRecord(String nationalId) {
        try {
            var response = restTemplate.exchange(
                    bridgeBaseUrl + "/api/v1/exchange/data/" + nationalId + "/PROPERTY_RECORD",
                    HttpMethod.GET, new HttpEntity<>(headers()),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return response.getBody() != null && !response.getBody().isEmpty();
        } catch (Exception ex) {
            log.warn("Bridge PROPERTY_RECORD query failed for {}: {}", nationalId, ex.getMessage());
            return false;
        }
    }
}
