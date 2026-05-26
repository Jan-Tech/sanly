package com.sanly.land.client;

import com.sanly.land.entity.Ownership;
import com.sanly.land.entity.Property;
import com.sanly.land.repository.OwnershipRepository;
import com.sanly.land.repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class BridgePublisherService {
    private static final Logger log = LoggerFactory.getLogger(BridgePublisherService.class);

    private final RestTemplate restTemplate;
    private final PropertyRepository propertyRepository;
    private final OwnershipRepository ownershipRepository;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   PropertyRepository propertyRepository,
                                   OwnershipRepository ownershipRepository,
                                   @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${sanly.bridge.institution-code}") String institutionCode,
                                   @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.propertyRepository = propertyRepository;
        this.ownershipRepository = ownershipRepository;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishOwnership(UUID ownershipId) {
        try {
            Ownership o = ownershipRepository.findById(ownershipId).orElse(null);
            if (o == null) return;
            Property p = propertyRepository.findByCadastralNumber(o.getCadastralNumber()).orElse(null);
            if (p == null) return;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "subjectNationalId", o.getOwnerNationalId(),
                    "dataType", "PROPERTY_RECORD",
                    "recordRef", o.getCadastralNumber(),
                    "payload", Map.of(
                            "cadastralNumber", o.getCadastralNumber(),
                            "ownerNationalId", o.getOwnerNationalId(),
                            "ownershipShare",  o.getOwnershipShare(),
                            "propertyType",    p.getPropertyType().name(),
                            "region",          p.getRegion(),
                            "acquiredAt",      o.getAcquiredAt().toString(),
                            "acquiredVia",     o.getAcquiredVia().name(),
                            "status",          o.getStatus().name()
                    )
            );

            restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Void.class);
            log.info("Published PROPERTY_RECORD for {} to bridge", o.getCadastralNumber());
        } catch (Exception ex) {
            log.warn("Bridge publish failed for ownership {}: {}", ownershipId, ex.getMessage());
        }
    }
}
