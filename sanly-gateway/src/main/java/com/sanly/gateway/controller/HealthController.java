package com.sanly.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);
    private static final Duration PING_TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient = WebClient.builder().build();

    // Service name → base URL
    private final Map<String, String> serviceUrls;

    public HealthController(
            @Value("${services.registry-url}") String registryUrl,
            @Value("${services.bridge-url}") String bridgeUrl,
            @Value("${services.medical-url}") String medicalUrl,
            @Value("${services.dmv-url}") String dmvUrl,
            @Value("${services.police-url}") String policeUrl,
            @Value("${services.tax-url}") String taxUrl,
            @Value("${services.business-url}") String businessUrl,
            @Value("${services.civil-url}") String civilUrl,
            @Value("${services.notifications-url}") String notificationsUrl,
            @Value("${services.pharmacy-url}") String pharmacyUrl,
            @Value("${services.education-url}") String educationUrl,
            @Value("${services.land-url}") String landUrl,
            @Value("${services.social-url}") String socialUrl,
            @Value("${services.customs-url}") String customsUrl,
            @Value("${services.court-url}") String courtUrl,
            @Value("${services.vehicle-url}") String vehicleUrl,
            @Value("${services.pension-url}") String pensionUrl) {

        serviceUrls = new LinkedHashMap<>();
        serviceUrls.put("citizen-registry", registryUrl);
        serviceUrls.put("sanly-bridge", bridgeUrl);
        serviceUrls.put("sanly-medical", medicalUrl);
        serviceUrls.put("sanly-dmv", dmvUrl);
        serviceUrls.put("sanly-police", policeUrl);
        serviceUrls.put("sanly-tax", taxUrl);
        serviceUrls.put("sanly-business", businessUrl);
        serviceUrls.put("sanly-civil", civilUrl);
        serviceUrls.put("sanly-notifications", notificationsUrl);
        serviceUrls.put("sanly-pharmacy", pharmacyUrl);
        serviceUrls.put("sanly-education", educationUrl);
        serviceUrls.put("sanly-land", landUrl);
        serviceUrls.put("sanly-social", socialUrl);
        serviceUrls.put("sanly-customs", customsUrl);
        serviceUrls.put("sanly-court", courtUrl);
        serviceUrls.put("sanly-vehicle", vehicleUrl);
        serviceUrls.put("sanly-pension", pensionUrl);
    }

    @GetMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> servicesHealth() {
        // Ping all services' actuator health in parallel, aggregate results
        var checks = serviceUrls.entrySet().stream()
                .map(e -> pingService(e.getKey(), e.getValue()))
                .toList();

        return Mono.zip(checks, results -> {
            Map<String, Object> response = new LinkedHashMap<>();
            boolean allUp = true;
            Map<String, String> statuses = new LinkedHashMap<>();
            for (Object r : results) {
                @SuppressWarnings("unchecked")
                Map<String, String> entry = (Map<String, String>) r;
                statuses.putAll(entry);
                if (entry.values().stream().anyMatch("DOWN"::equals)) {
                    allUp = false;
                }
            }
            response.put("gateway", "UP");
            response.put("overall", allUp ? "UP" : "DEGRADED");
            response.put("services", statuses);
            return response;
        });
    }

    private Mono<Map<String, String>> pingService(String name, String baseUrl) {
        return webClient.get()
                .uri(baseUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(PING_TIMEOUT)
                .map(body -> Map.of(name, "UP"))
                .onErrorResume(ex -> {
                    log.warn("Health check failed for {}: {}", name, ex.getMessage());
                    return Mono.just(Map.of(name, "DOWN"));
                });
    }
}
