package com.sanly.business.client;

import com.sanly.business.entity.Business;
import com.sanly.business.repository.BusinessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class BridgePublisherService {
    private static final Logger log = LoggerFactory.getLogger(BridgePublisherService.class);

    private final RestTemplate restTemplate;
    private final BusinessRepository businessRepository;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   BusinessRepository businessRepository,
                                   @Value("${bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${bridge.institution-code}") String institutionCode,
                                   @Value("${bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.businessRepository = businessRepository;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishBusinessRegistration(UUID businessId) {
        try {
            Business business = businessRepository.findById(businessId).orElse(null);
            if (business == null) return;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "subjectNationalId", business.getOwnerNationalId(),
                    "dataType", "BUSINESS_REGISTRATION",
                    "recordRef", business.getRegistrationNumber(),
                    "payload", Map.of(
                            "registrationNumber", business.getRegistrationNumber(),
                            "businessName", business.getBusinessName(),
                            "businessType", business.getBusinessType().name(),
                            "status", business.getStatus().name(),
                            "registrationDate", business.getRegistrationDate().toString()
                    )
            );

            restTemplate.postForEntity(
                    bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers),
                    Void.class);

            business.setBridgePublished(true);
            businessRepository.save(business);
            log.info("Published BUSINESS_REGISTRATION for {} to bridge", business.getRegistrationNumber());
        } catch (Exception ex) {
            log.warn("Bridge publish failed for business {}: {}", businessId, ex.getMessage());
        }
    }
}
