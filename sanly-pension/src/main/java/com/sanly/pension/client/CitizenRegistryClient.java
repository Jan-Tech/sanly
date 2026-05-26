package com.sanly.pension.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.pension.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Slf4j
@Component
public class CitizenRegistryClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public CitizenRegistryClient(RestTemplate restTemplate,
                                  @Value("${sanly.citizen-registry.base-url}") String baseUrl,
                                  @Value("${sanly.citizen-registry.institution-token}") String token) {
        this.restTemplate = restTemplate; this.baseUrl = baseUrl; this.token = token;
    }

    public LocalDate verifyAndGetBirthDate(String nationalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        try {
            ResponseEntity<JsonNode> resp = restTemplate.exchange(
                    baseUrl + "/api/v1/citizens/" + nationalId,
                    HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            JsonNode body = resp.getBody();
            if (body != null && body.has("dateOfBirth")) {
                return LocalDate.parse(body.get("dateOfBirth").asText());
            }
            return null;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Citizen not found: " + nationalId);
        } catch (Exception e) {
            log.warn("Citizen registry unavailable, proceeding: {}", e.getMessage());
            return null;
        }
    }

    public void verify(String nationalId) {
        verifyAndGetBirthDate(nationalId);
    }
}
