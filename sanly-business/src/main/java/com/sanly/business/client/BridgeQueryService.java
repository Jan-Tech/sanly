package com.sanly.business.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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
                               @Value("${bridge.base-url}") String bridgeBaseUrl,
                               @Value("${bridge.institution-code}") String institutionCode,
                               @Value("${bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    public List<Map<String, Object>> query(String nationalId, String dataType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Institution-Code", institutionCode);
        headers.set("X-Institution-Key", institutionKey);
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    bridgeBaseUrl + "/api/v1/exchange/data/" + nationalId + "/" + dataType,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {});
            return resp.getBody() != null ? resp.getBody() : List.of();
        } catch (HttpClientErrorException.Forbidden ex) {
            log.warn("Bridge permission denied for {}/{}", nationalId, dataType);
            return List.of();
        } catch (Exception ex) {
            log.warn("Bridge query failed for {}/{}: {}", nationalId, dataType, ex.getMessage());
            return List.of();
        }
    }
}
