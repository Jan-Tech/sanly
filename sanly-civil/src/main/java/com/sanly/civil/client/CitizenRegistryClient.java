package com.sanly.civil.client;

import com.sanly.civil.exception.CitizenNotFoundException;
import com.sanly.civil.exception.CitizenRegistryUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CitizenRegistryClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;
    private final String adminToken;

    public CitizenRegistryClient(RestTemplate restTemplate,
                                  @Value("${citizen-registry.base-url}") String baseUrl,
                                  @Value("${citizen-registry.token}") String token,
                                  @Value("${citizen-registry.admin-token}") String adminToken) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.token = token;
        this.adminToken = adminToken;
    }

    public void verify(String nationalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        try {
            restTemplate.exchange(
                    baseUrl + "/api/v1/citizens/" + nationalId + "/verify",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Void.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CitizenNotFoundException("Citizen not found: " + nationalId);
        } catch (Exception ex) {
            throw new CitizenRegistryUnavailableException("Citizen Registry unavailable: " + ex.getMessage());
        }
    }

    public void markDeceased(String nationalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        try {
            restTemplate.exchange(
                    baseUrl + "/api/v1/citizens/" + nationalId + "/status",
                    HttpMethod.PATCH,
                    new HttpEntity<>(Map.of("status", "DECEASED"), headers),
                    Void.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CitizenNotFoundException("Citizen not found: " + nationalId);
        } catch (Exception ex) {
            throw new CitizenRegistryUnavailableException("Could not mark citizen as deceased: " + ex.getMessage());
        }
    }
}
