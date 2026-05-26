package com.sanly.social.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class BridgeQueryService {
    private static final Logger log = LoggerFactory.getLogger(BridgeQueryService.class);
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

    public boolean isTaxNonCompliant(String nationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            var response = restTemplate.exchange(
                    bridgeBaseUrl + "/api/v1/exchange/data/" + nationalId + "/TAX_STATUS",
                    HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> records = response.getBody();
            if (records == null || records.isEmpty()) return false;
            Object payload = records.get(0).get("payload");
            if (payload instanceof Map<?, ?> p) {
                return "NON_COMPLIANT".equals(String.valueOf(p.get("complianceStatus")));
            }
            return false;
        } catch (Exception ex) {
            log.warn("Bridge TAX_STATUS query failed for {}: {} — allowing claim", nationalId, ex.getMessage());
            return false;
        }
    }
}
