package com.sanly.dmv.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.dmv.dto.response.CitizenVerifyResponse;
import com.sanly.dmv.exception.CitizenNotFoundException;
import com.sanly.dmv.exception.CitizenRegistryUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CitizenRegistryClient {

    private final RestTemplate restTemplate;
    private final String       baseUrl;
    private final String       institutionToken;

    public CitizenRegistryClient(
            RestTemplate restTemplate,
            @Value("${sanly.citizen-registry.base-url}")          String baseUrl,
            @Value("${sanly.citizen-registry.institution-token}") String institutionToken) {
        this.restTemplate     = restTemplate;
        this.baseUrl          = baseUrl;
        this.institutionToken = institutionToken;
    }

    public CitizenVerifyResponse verify(String nationalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(institutionToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    baseUrl + "/api/v1/citizens/{nationalId}/verify",
                    HttpMethod.GET, entity, JsonNode.class, nationalId);

            JsonNode body = response.getBody();
            if (body == null || !body.path("success").asBoolean(false)) {
                throw new CitizenNotFoundException(nationalId);
            }
            JsonNode data = body.path("data");
            if (!data.path("exists").asBoolean(false)) {
                throw new CitizenNotFoundException(nationalId);
            }
            if (!data.path("active").asBoolean(false)) {
                throw new CitizenNotFoundException("Citizen " + nationalId + " is not ACTIVE");
            }
            return CitizenVerifyResponse.builder()
                    .nationalId(nationalId).exists(true).active(true)
                    .status(data.path("status").asText())
                    .fullName(data.path("fullName").asText(null))
                    .build();

        } catch (CitizenNotFoundException | CitizenRegistryUnavailableException e) { throw e; }
        catch (HttpClientErrorException.NotFound e) { throw new CitizenNotFoundException(nationalId); }
        catch (RestClientException e) {
            log.error("Citizen Registry unavailable for NIN {}: {}", nationalId, e.getMessage());
            throw new CitizenRegistryUnavailableException(
                    "SANLY Citizen Registry is currently unavailable. Try again later.");
        }
    }
}
