package com.sanly.police.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sanly.police.exception.BridgeUnavailableException;
import com.sanly.police.exception.InsufficientPermissionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Queries SANLY Bridge for citizen data. Used by citizen-check endpoint.
 * Returns empty list if bridge has no records (NOT_FOUND is not an error).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BridgeQueryService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Value("${sanly.bridge.base-url}") private String baseUrl;
    @Value("${sanly.bridge.institution-code}") private String institutionCode;
    @Value("${sanly.bridge.institution-key}") private String institutionKey;

    /**
     * Queries the bridge for a specific data type for a citizen.
     * Returns parsed list of record maps. Empty list if none found.
     */
    public List<Map<String, Object>> query(String nationalId, String dataType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Institution-Code", institutionCode);
        headers.set("X-Institution-Key", institutionKey);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    baseUrl + "/api/v1/exchange/data/{nationalId}/{dataType}",
                    HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class, nationalId, dataType);

            JsonNode body = response.getBody();
            if (body == null || !body.path("success").asBoolean()) return List.of();

            JsonNode data = body.path("data");
            List<Map<String, Object>> result = new ArrayList<>();
            if (data.isArray()) {
                for (JsonNode node : data) {
                    result.add(objectMapper.convertValue(node, new TypeReference<>() {}));
                }
            }
            return result;
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Bridge permission denied for {} querying {}", institutionCode, dataType);
            throw new InsufficientPermissionException(
                    institutionCode + " does not have permission to query " + dataType);
        } catch (HttpClientErrorException.NotFound e) {
            return List.of();
        } catch (Exception e) {
            log.error("Bridge unavailable for {}/{}: {}", nationalId, dataType, e.getMessage());
            throw new BridgeUnavailableException("SANLY Bridge unavailable, please try again shortly");
        }
    }
}
