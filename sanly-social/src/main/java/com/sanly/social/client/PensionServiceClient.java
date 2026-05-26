package com.sanly.social.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Delegates pension eligibility queries to sanly-pension (the authoritative pension service).
 * sanly-social's own pension logic is deprecated — use sanly-pension instead.
 * See sanly-social/README.md for the migration path.
 */
@Slf4j
@Component
public class PensionServiceClient {

    private final RestTemplate restTemplate;
    private final String pensionBaseUrl;
    private final String institutionToken;

    public PensionServiceClient(RestTemplate restTemplate,
                                 @Value("${sanly.pension.base-url:http://localhost:8099}") String pensionBaseUrl,
                                 @Value("${sanly.pension.institution-token:placeholder}") String institutionToken) {
        this.restTemplate = restTemplate;
        this.pensionBaseUrl = pensionBaseUrl;
        this.institutionToken = institutionToken;
    }

    /**
     * Returns true if the citizen has a pension account that is ELIGIBLE or PAYING.
     * Returns false (non-fatal) if pension service is unavailable.
     */
    public boolean isPensionEligible(String nationalId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + institutionToken);
            ResponseEntity<JsonNode> resp = restTemplate.exchange(
                    pensionBaseUrl + "/api/v1/pension/accounts/citizen/" + nationalId + "/eligibility",
                    HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
            JsonNode body = resp.getBody();
            return body != null && body.path("eligible").asBoolean(false);
        } catch (Exception e) {
            log.warn("[PENSION-CLIENT] Could not check pension eligibility for NIN={}: {}", nationalId, e.getMessage());
            return false;
        }
    }
}
