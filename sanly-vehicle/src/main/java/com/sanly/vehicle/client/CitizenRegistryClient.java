package com.sanly.vehicle.client;

import com.sanly.vehicle.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CitizenRegistryClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public CitizenRegistryClient(RestTemplate restTemplate,
                                  @Value("${sanly.citizen-registry.base-url}") String baseUrl,
                                  @Value("${sanly.citizen-registry.institution-token}") String token) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public void verify(String nationalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        try {
            restTemplate.exchange(baseUrl + "/api/v1/citizens/" + nationalId,
                    HttpMethod.GET, new HttpEntity<>(headers), Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Citizen not found: " + nationalId);
        } catch (Exception e) {
            log.warn("Citizen registry unavailable, proceeding: {}", e.getMessage());
        }
    }
}
