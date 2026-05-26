package com.sanly.signature.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class BridgeClient {

    private static final Logger log = LoggerFactory.getLogger(BridgeClient.class);

    private final RestTemplate restTemplate;
    private final String bridgeBaseUrl;
    private final String institutionKey;

    public BridgeClient(RestTemplate restTemplate,
                        @Value("${bridge.base-url}") String bridgeBaseUrl,
                        @Value("${bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionKey = institutionKey;
    }

    /**
     * Publishes a digital signature event to the sanly-bridge data exchange.
     * Runs asynchronously — failures are logged but do not affect the signing transaction.
     */
    @Async("signatureExecutor")
    public void publishSignature(String signatureCode,
                                 String signerNationalId,
                                 LocalDateTime signedAt,
                                 String purpose,
                                 String status) {
        try {
            if (bridgeBaseUrl == null || bridgeBaseUrl.isBlank()) {
                log.warn("Bridge URL not configured — skipping publish for signatureCode={}", signatureCode);
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "subjectNationalId", signerNationalId,
                    "dataType", "DIGITAL_SIGNATURE",
                    "recordRef", signatureCode,
                    "payload", Map.of(
                            "signatureCode", signatureCode,
                            "signerNationalId", signerNationalId,
                            "signedAt", signedAt.toString(),
                            "purpose", purpose != null ? purpose : "",
                            "status", status
                    )
            );

            restTemplate.postForEntity(
                    bridgeBaseUrl + "/api/v1/exchange",
                    new HttpEntity<>(body, headers),
                    Void.class);

            log.info("Published DIGITAL_SIGNATURE to bridge: signatureCode={}", signatureCode);
        } catch (Exception e) {
            log.warn("Bridge publish failed for signatureCode={}: {}", signatureCode, e.getMessage());
        }
    }
}
