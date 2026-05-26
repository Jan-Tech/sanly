package com.sanly.customs.client;

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
        this.restTemplate = restTemplate;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Institution-Code", institutionCode);
        h.set("X-Institution-Key", institutionKey);
        return h;
    }

    public boolean isBusinessActive(String businessNumber) {
        try {
            var response = restTemplate.exchange(
                    bridgeBaseUrl + "/api/v1/exchange/data/" + businessNumber + "/BUSINESS_REGISTRATION",
                    HttpMethod.GET, new HttpEntity<>(headers()),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> records = response.getBody();
            if (records == null || records.isEmpty()) return false;
            Object payload = records.get(0).get("payload");
            if (payload instanceof Map<?, ?> p)
                return "ACTIVE".equals(String.valueOf(p.get("status")));
            return false;
        } catch (Exception ex) {
            log.warn("Bridge BUSINESS_REGISTRATION query failed for {}: {}", businessNumber, ex.getMessage());
            return false;
        }
    }

    public boolean isTaxNonCompliant(String subjectId) {
        try {
            var response = restTemplate.exchange(
                    bridgeBaseUrl + "/api/v1/exchange/data/" + subjectId + "/TAX_STATUS",
                    HttpMethod.GET, new HttpEntity<>(headers()),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> records = response.getBody();
            if (records == null || records.isEmpty()) return false;
            Object payload = records.get(0).get("payload");
            if (payload instanceof Map<?, ?> p)
                return "NON_COMPLIANT".equals(String.valueOf(p.get("complianceStatus")));
            return false;
        } catch (Exception ex) {
            log.warn("Bridge TAX_STATUS query failed for {}: {} — allowing clearance", subjectId, ex.getMessage());
            return false;
        }
    }
}
