package com.sanly.customs.client;

import com.sanly.customs.entity.CustomsDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BridgePublisherService {
    private final RestTemplate restTemplate;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${sanly.bridge.institution-code}") String institutionCode,
                                   @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishClearance(CustomsDeclaration declaration) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Subject is NIN for citizens, business number for businesses
            String subjectId = declaration.getDeclarantNationalId() != null
                    ? declaration.getDeclarantNationalId()
                    : declaration.getDeclarantBusinessNumber();

            Map<String, Object> payload = new HashMap<>();
            payload.put("declarationCode", declaration.getDeclarationCode());
            payload.put("declarantNationalId", declaration.getDeclarantNationalId());
            payload.put("declarantBusinessNumber", declaration.getDeclarantBusinessNumber());
            payload.put("declarationType", declaration.getDeclarationType().name());
            payload.put("portCode", declaration.getPortCode());
            payload.put("hsCode", declaration.getHsCode());
            payload.put("clearedAt", LocalDateTime.now().toString());
            payload.put("dutiesPaid", declaration.getDutiesPaid());

            Map<String, Object> body = Map.of(
                    "subjectNationalId", subjectId,
                    "dataType", "CUSTOMS_CLEARANCE",
                    "recordRef", declaration.getDeclarationCode(),
                    "payload", payload
            );

            restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Void.class);
            log.info("[BRIDGE] Published CUSTOMS_CLEARANCE for {}", declaration.getDeclarationCode());
        } catch (Exception ex) {
            log.warn("[BRIDGE] Failed to publish CUSTOMS_CLEARANCE for {}: {}",
                    declaration.getDeclarationCode(), ex.getMessage());
        }
    }
}
