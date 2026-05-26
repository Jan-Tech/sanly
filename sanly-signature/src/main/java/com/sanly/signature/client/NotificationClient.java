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

import java.util.Map;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final String notificationBaseUrl;
    private final String serviceKey;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification.base-url}") String notificationBaseUrl,
                              @Value("${notification.service-key}") String serviceKey) {
        this.restTemplate = restTemplate;
        this.notificationBaseUrl = notificationBaseUrl;
        this.serviceKey = serviceKey;
    }

    /**
     * Sends a notification to the citizen when their document is signed.
     */
    @Async("signatureExecutor")
    public void notifyDocumentSigned(String nationalId,
                                     String signatureCode,
                                     String purpose,
                                     String language) {
        try {
            send(nationalId, "DOCUMENT_SIGNED", language,
                    Map.of(
                            "signatureCode", signatureCode,
                            "purpose", purpose != null ? purpose : ""
                    ));
        } catch (Exception e) {
            log.warn("Notify DOCUMENT_SIGNED failed for NIN={}: {}", nationalId, e.getMessage());
        }
    }

    /**
     * Sends a notification to the citizen when their signature is revoked.
     */
    @Async("signatureExecutor")
    public void notifySignatureRevoked(String nationalId,
                                       String signatureCode,
                                       String reason,
                                       String language) {
        try {
            send(nationalId, "SIGNATURE_REVOKED", language,
                    Map.of(
                            "signatureCode", signatureCode,
                            "reason", reason != null ? reason : ""
                    ));
        } catch (Exception e) {
            log.warn("Notify SIGNATURE_REVOKED failed for NIN={}: {}", nationalId, e.getMessage());
        }
    }

    private void send(String nationalId, String eventType, String language,
                      Map<String, String> metadata) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Service-Name", "SANLY_SIGNATURE");
        headers.set("X-Service-Key", serviceKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(
                notificationBaseUrl + "/api/v1/notifications/send",
                new HttpEntity<>(Map.of(
                        "citizenNationalId", nationalId,
                        "eventType", eventType,
                        "language", language != null ? language : "tk",
                        "metadata", metadata != null ? metadata : Map.of()
                ), headers),
                Void.class);

        log.info("Notification dispatched: event={} NIN={}", eventType, nationalId);
    }
}
