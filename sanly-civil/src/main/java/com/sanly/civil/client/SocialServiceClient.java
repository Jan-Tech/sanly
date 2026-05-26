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
public class SocialServiceClient {

    private final RestTemplate restTemplate;
    private final String socialBaseUrl;
    private final String lifeEventKey;

    public SocialServiceClient(RestTemplate restTemplate,
                                @Value("${life-event.social-url:http://localhost:8092}") String socialBaseUrl,
                                @Value("${life-event.service-key:change-me-life-event-key}") String lifeEventKey) {
        this.restTemplate = restTemplate;
        this.socialBaseUrl = socialBaseUrl;
        this.lifeEventKey = lifeEventKey;
    }

    @Async("lifeEventExecutor")
    public void autoTriggerBenefit(String nationalId, String benefitType, String triggerReason) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    socialBaseUrl + "/api/v1/social/claims/auto-trigger",
                    new HttpEntity<>(Map.of("citizenNationalId", nationalId,
                            "benefitType", benefitType,
                            "triggerReason", triggerReason), headers),
                    Void.class);
            log.info("[SOCIAL] Auto-triggered {} benefit for NIN={}", benefitType, nationalId);
        } catch (Exception ex) {
            log.warn("[SOCIAL] Failed to auto-trigger {} benefit for NIN={}: {}",
                    benefitType, nationalId, ex.getMessage());
        }
    }

    @Async("lifeEventExecutor")
    public void cancelAllBenefits(String nationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    socialBaseUrl + "/api/v1/social/claims/cancel-all",
                    new HttpEntity<>(Map.of("citizenNationalId", nationalId), headers),
                    Void.class);
            log.info("[SOCIAL] All benefits cancelled for deceased NIN={}", nationalId);
        } catch (Exception ex) {
            log.warn("[SOCIAL] Failed to cancel benefits for NIN={}: {}", nationalId, ex.getMessage());
        }
    }

    @Async("lifeEventExecutor")
    public void triggerSurvivorBenefit(String spouseNationalId, String deceasedNationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Life-Event-Key", lifeEventKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(
                    socialBaseUrl + "/api/v1/social/claims/auto-trigger",
                    new HttpEntity<>(Map.of("citizenNationalId", spouseNationalId,
                            "benefitType", "SURVIVOR",
                            "triggerReason", "Spouse deceased: " + deceasedNationalId), headers),
                    Void.class);
            log.info("[SOCIAL] Survivor benefit triggered for NIN={} (deceased={})",
                    spouseNationalId, deceasedNationalId);
        } catch (Exception ex) {
            log.warn("[SOCIAL] Failed to trigger survivor benefit for NIN={}: {}",
                    spouseNationalId, ex.getMessage());
        }
    }
}
