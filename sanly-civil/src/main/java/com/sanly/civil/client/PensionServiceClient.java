package com.sanly.civil.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class PensionServiceClient {

    private final RestTemplate restTemplate;
    private final String pensionBaseUrl;
    private final String lifeEventKey;

    public PensionServiceClient(RestTemplate restTemplate,
                                 @Value("${life-event.pension-url:http://localhost:8099}") String pensionBaseUrl,
                                 @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate = restTemplate;
        this.pensionBaseUrl = pensionBaseUrl;
        this.lifeEventKey = lifeEventKey;
    }

    @Async("lifeEventExecutor")
    public void handleOwnerDeceased(String deceasedNationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    pensionBaseUrl + "/api/v1/pension/accounts/deceased",
                    new HttpEntity<>(Map.of("deceasedNationalId", deceasedNationalId), headers),
                    Void.class);
            log.info("[PENSION] Deceased account closure triggered for NIN={}", deceasedNationalId);
        } catch (Exception ex) {
            log.warn("[PENSION] Failed to handle deceased account for NIN={}: {}",
                    deceasedNationalId, ex.getMessage());
        }
    }
}
