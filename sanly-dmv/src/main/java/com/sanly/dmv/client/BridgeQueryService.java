package com.sanly.dmv.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.dmv.exception.BridgeUnavailableException;
import com.sanly.dmv.exception.InsufficientPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Queries SANLY Bridge for published inter-agency data.
 *
 * Unlike {@code BridgePublisherService} in sanly-medical (which pushes data),
 * this service PULLS data — it's a consumer of Bridge records.
 *
 * Error handling:
 * - 403 Forbidden → {@link InsufficientPermissionException} (INST_DMV lacks permission)
 * - Connection/5xx  → {@link BridgeUnavailableException} (503 returned to caller)
 */
@Slf4j
@Service
public class BridgeQueryService {

    private static final String DATA_PATH = "/api/v1/exchange/data/{nationalId}/{dataType}";

    private final RestTemplate restTemplate;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgeQueryService(
            RestTemplate restTemplate,
            @Value("${sanly.bridge.base-url}")         String bridgeBaseUrl,
            @Value("${sanly.bridge.institution-code}") String institutionCode,
            @Value("${sanly.bridge.institution-key}")  String institutionKey) {
        this.restTemplate    = restTemplate;
        this.bridgeBaseUrl   = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey  = institutionKey;
    }

    /**
     * Returns all active, non-expired vision test records for the given citizen.
     * Sorted by publishedAt descending (most recent first) by the bridge.
     */
    public List<BridgeRecord> queryVisionTests(String nationalId) {
        return query(nationalId, "VISION_TEST");
    }

    /**
     * Generic bridge data query — INST_DMV must have a permission grant for the dataType.
     */
    public List<BridgeRecord> query(String nationalId, String dataType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Institution-Code", institutionCode);
        headers.set("X-Institution-Key",  institutionKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    bridgeBaseUrl + DATA_PATH,
                    HttpMethod.GET, entity, JsonNode.class,
                    nationalId, dataType);

            JsonNode body = response.getBody();
            if (body == null || !body.path("success").asBoolean(false)) return List.of();

            JsonNode dataArray = body.path("data");
            List<BridgeRecord> records = new ArrayList<>();
            for (JsonNode node : dataArray) {
                records.add(parseRecord(node));
            }
            return records;

        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Bridge denied INST_DMV access to {} data: {}", dataType, e.getMessage());
            throw new InsufficientPermissionException(
                    "INST_DMV does not have permission to query " + dataType + " data from SANLY Bridge");
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Bridge rejected INST_DMV API key");
            throw new BridgeUnavailableException(
                    "SANLY Bridge authentication failed — check BRIDGE_INSTITUTION_KEY configuration");
        } catch (RestClientException e) {
            log.error("Bridge unavailable while querying {} for {}: {}", dataType, nationalId, e.getMessage());
            throw new BridgeUnavailableException(
                    "SANLY Bridge unavailable, please try again shortly");
        }
    }

    // ---- private ----

    private BridgeRecord parseRecord(JsonNode node) {
        BridgeRecord record = new BridgeRecord();
        record.setId(node.path("id").asText(null));
        record.setPublisherCode(node.path("publisherCode").asText(null));
        record.setNationalId(node.path("nationalId").asText(null));
        record.setDataType(node.path("dataType").asText(null));
        record.setRecordRef(node.path("recordRef").asText(null));
        record.setActive(node.path("active").asBoolean(true));

        // Parse expiresAt from ISO string
        String expiresAtStr = node.path("expiresAt").asText(null);
        if (expiresAtStr != null && !expiresAtStr.isBlank() && !"null".equals(expiresAtStr)) {
            try {
                record.setExpiresAt(LocalDateTime.parse(expiresAtStr));
            } catch (DateTimeParseException e) {
                log.warn("Could not parse expiresAt '{}': {}", expiresAtStr, e.getMessage());
            }
        }

        // Parse summary JSON object into a flat Map
        JsonNode summaryNode = node.path("summary");
        if (!summaryNode.isMissingNode() && !summaryNode.isNull()) {
            Map<String, Object> summary = new HashMap<>();
            summaryNode.fields().forEachRemaining(entry ->
                    summary.put(entry.getKey(), entry.getValue().asText(null)));
            record.setSummary(summary);
        }

        return record;
    }
}
