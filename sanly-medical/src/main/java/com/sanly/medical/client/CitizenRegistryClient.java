package com.sanly.medical.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.medical.dto.response.CitizenVerifyResponse;
import com.sanly.medical.exception.CitizenNotFoundException;
import com.sanly.medical.exception.CitizenRegistryUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for the SANLY Citizen Registry service.
 *
 * Uses a ROLE_INSTITUTION JWT configured via {@code sanly.citizen-registry.institution-token}.
 * Calls the verify endpoint — a lightweight check that returns existence + active status
 * without returning full PII (suitable for ROLE_INSTITUTION access).
 */
@Slf4j
@Component
public class CitizenRegistryClient {

    private final RestTemplate restTemplate;
    private final String       baseUrl;
    private final String       institutionToken;

    public CitizenRegistryClient(
            RestTemplate restTemplate,
            @Value("${sanly.citizen-registry.base-url}")        String baseUrl,
            @Value("${sanly.citizen-registry.institution-token}") String institutionToken) {
        this.restTemplate      = restTemplate;
        this.baseUrl           = baseUrl;
        this.institutionToken  = institutionToken;
    }

    /**
     * Verifies a citizen exists and is ACTIVE in the SANLY Citizen Registry.
     *
     * @param nationalId the 11-digit TM-NIN to check
     * @return verified citizen info
     * @throws CitizenNotFoundException          if the citizen doesn't exist or is not ACTIVE
     * @throws CitizenRegistryUnavailableException if the registry service is unreachable
     */
    public CitizenVerifyResponse verify(String nationalId) {
        String url = baseUrl + "/api/v1/citizens/{nationalId}/verify";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(institutionToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JsonNode.class, nationalId);

            JsonNode body = response.getBody();
            if (body == null || !body.path("success").asBoolean(false)) {
                throw new CitizenNotFoundException(nationalId);
            }

            JsonNode data = body.path("data");
            boolean exists = data.path("exists").asBoolean(false);
            boolean active = data.path("active").asBoolean(false);

            if (!exists) {
                throw new CitizenNotFoundException(nationalId);
            }
            if (!active) {
                throw new CitizenNotFoundException(
                        "Citizen " + nationalId + " exists but is not ACTIVE (status: "
                        + data.path("status").asText("UNKNOWN") + ")");
            }

            return CitizenVerifyResponse.builder()
                    .nationalId(nationalId)
                    .exists(true)
                    .active(true)
                    .status(data.path("status").asText())
                    .fullName(data.path("fullName").asText(null))
                    .build();

        } catch (CitizenNotFoundException | CitizenRegistryUnavailableException e) {
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            throw new CitizenNotFoundException(nationalId);
        } catch (RestClientException e) {
            log.error("Citizen Registry unreachable for NIN {}: {}", nationalId, e.getMessage());
            throw new CitizenRegistryUnavailableException(
                    "SANLY Citizen Registry is currently unavailable. Try again later.");
        }
    }
}
