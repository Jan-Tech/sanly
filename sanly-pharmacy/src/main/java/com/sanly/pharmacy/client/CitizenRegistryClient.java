package com.sanly.pharmacy.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class CitizenRegistryClient {

    private final RestTemplate restTemplate;
    private final String       baseUrl;
    private final String       token;

    public CitizenRegistryClient(RestTemplate restTemplate,
                                  @Value("${sanly.citizen-registry.base-url}") String baseUrl,
                                  @Value("${sanly.citizen-registry.institution-token}") String token) {
        this.restTemplate = restTemplate;
        this.baseUrl      = baseUrl;
        this.token        = token;
    }

    /** Returns citizen's firstName + " " + lastName, or null if unreachable. */
    public String getCitizenFullName(String nationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            ResponseEntity<Map> resp = restTemplate.exchange(
                    baseUrl + "/api/v1/citizens/" + nationalId,
                    HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<?, ?> body = resp.getBody();
                return body.get("firstName") + " " + body.get("lastName");
            }
        } catch (Exception ex) {
            log.warn("Citizen registry lookup failed for NIN={}: {}", nationalId, ex.getMessage());
        }
        return null;
    }
}
