package com.sanly.land.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CitizenRegistryClient {
    private static final Logger log = LoggerFactory.getLogger(CitizenRegistryClient.class);

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
        headers.setBearerAuth(token);
        restTemplate.exchange(baseUrl + "/api/v1/citizens/" + nationalId,
                HttpMethod.GET, new HttpEntity<>(headers), Void.class);
        log.debug("Citizen {} verified", nationalId);
    }
}
