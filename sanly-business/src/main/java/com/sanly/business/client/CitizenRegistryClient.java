package com.sanly.business.client;

import com.sanly.business.exception.CitizenNotFoundException;
import com.sanly.business.exception.CitizenRegistryUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CitizenRegistryClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public CitizenRegistryClient(RestTemplate restTemplate,
                                  @Value("${citizen-registry.base-url}") String baseUrl,
                                  @Value("${citizen-registry.token}") String token) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.token = token;
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
}
