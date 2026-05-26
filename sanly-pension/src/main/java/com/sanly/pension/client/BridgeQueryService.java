package com.sanly.pension.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BridgeQueryService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String baseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgeQueryService(RestTemplate restTemplate,
                               @Value("${sanly.bridge.base-url}") String baseUrl,
                               @Value("${sanly.bridge.institution-code}") String institutionCode,
                               @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate; this.baseUrl = baseUrl;
        this.institutionCode = institutionCode; this.institutionKey = institutionKey;
    }

    public List<Map<String, Object>> query(String nationalId, String dataType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Institution-Code", institutionCode);
        headers.set("X-Institution-Key", institutionKey);
        try {
            ResponseEntity<JsonNode> resp = restTemplate.exchange(
                    baseUrl + "/api/v1/exchange/data/{nationalId}/{dataType}",
                    HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class, nationalId, dataType);
            JsonNode body = resp.getBody();
            if (body == null || !body.path("success").asBoolean()) return List.of();
            JsonNode data = body.path("data");
            List<Map<String, Object>> result = new ArrayList<>();
            if (data.isArray())
                for (JsonNode node : data)
                    result.add(objectMapper.convertValue(node, new TypeReference<>() {}));
            return result;
        } catch (HttpClientErrorException.NotFound e) {
            return List.of();
        } catch (Exception e) {
            log.warn("Bridge query failed for {}/{}: {}", nationalId, dataType, e.getMessage());
            return List.of();
        }
    }

    public boolean isBusinessActive(String businessRegistrationNumber) {
        List<Map<String, Object>> records = query(businessRegistrationNumber, "BUSINESS_REGISTRATION");
        if (records.isEmpty()) return false;
        return records.stream().anyMatch(r -> "ACTIVE".equals(r.get("status")));
    }

    public boolean isEmployerTaxCompliant(String nationalId) {
        List<Map<String, Object>> records = query(nationalId, "TAX_STATUS");
        if (records.isEmpty()) return true;
        return records.stream().anyMatch(r -> "COMPLIANT".equals(r.get("complianceStatus")));
    }
}
