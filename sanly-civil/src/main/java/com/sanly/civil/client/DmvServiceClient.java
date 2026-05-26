package com.sanly.civil.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Calls sanly-dmv internal life-event endpoint to revoke deceased citizen's licenses.
 */
@Slf4j
@Component
public class DmvServiceClient {

    private final RestTemplate restTemplate;
    private final String       dmvBaseUrl;
    private final String       lifeEventKey;

    public DmvServiceClient(RestTemplate restTemplate,
                             @Value("${life-event.dmv-url:http://localhost:8083}") String dmvBaseUrl,
                             @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate = restTemplate;
        this.dmvBaseUrl   = dmvBaseUrl;
        this.lifeEventKey = lifeEventKey;
    }

    public void revokeDeceasedLicenses(String nationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    dmvBaseUrl + "/api/v1/licenses/suspend-deceased",
                    new HttpEntity<>(Map.of("nationalId", nationalId), headers),
                    Void.class);
            log.info("Deceased license revocation triggered for NIN={}", nationalId);
        } catch (Exception ex) {
            log.warn("Failed to revoke licenses for deceased NIN={}: {}", nationalId, ex.getMessage());
        }
    }
}
