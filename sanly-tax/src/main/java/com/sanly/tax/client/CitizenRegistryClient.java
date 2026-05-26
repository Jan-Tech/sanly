package com.sanly.tax.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.tax.exception.CitizenNotFoundException;
import com.sanly.tax.exception.CitizenRegistryUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j @Service @RequiredArgsConstructor
public class CitizenRegistryClient {

    private final RestTemplate restTemplate;
    @Value("${sanly.citizen-registry.base-url}") private String baseUrl;
    @Value("${sanly.citizen-registry.institution-token}") private String token;

    public void verify(String nationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    baseUrl + "/api/v1/citizens/{id}/verify",
                    HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class, nationalId);
            JsonNode data = response.getBody() != null ? response.getBody().path("data") : null;
            if (data == null || !data.path("exists").asBoolean(false)) throw new CitizenNotFoundException(nationalId);
        } catch (CitizenNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Citizen registry unavailable for {}: {}", nationalId, e.getMessage());
            throw new CitizenRegistryUnavailableException("SANLY Citizen Registry unavailable");
        }
    }
}
