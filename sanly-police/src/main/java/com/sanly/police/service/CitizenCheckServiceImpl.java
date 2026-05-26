package com.sanly.police.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.police.client.BridgeQueryService;
import com.sanly.police.client.CitizenRegistryClient;
import com.sanly.police.config.UserDetailsImpl;
import com.sanly.police.dto.request.CitizenCheckRequest;
import com.sanly.police.dto.response.CitizenCheckResponse;
import com.sanly.police.entity.CheckType;
import com.sanly.police.entity.CitizenCheck;
import com.sanly.police.exception.CitizenCheckNotFoundException;
import com.sanly.police.repository.CitizenCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenCheckServiceImpl {

    private final CitizenCheckRepository checkRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgeQueryService bridgeQueryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public CitizenCheckResponse performCheck(CitizenCheckRequest req) {
        citizenRegistryClient.verify(req.getCitizenNationalId());
        Long officerId = authenticatedOfficerId();

        Map<String, Object> results = new LinkedHashMap<>();

        if (req.getCheckType() == CheckType.DRIVING_LICENSE || req.getCheckType() == CheckType.FULL_CHECK) {
            List<Map<String, Object>> dlRecords = queryBridgeSafe(req.getCitizenNationalId(), "DRIVING_LICENSE");
            results.put("DRIVING_LICENSE", dlRecords);
        }
        if (req.getCheckType() == CheckType.TAX_STATUS || req.getCheckType() == CheckType.FULL_CHECK) {
            List<Map<String, Object>> taxRecords = queryBridgeSafe(req.getCitizenNationalId(), "TAX_STATUS");
            results.put("TAX_STATUS", taxRecords);
        }
        if (req.getCheckType() == CheckType.FULL_CHECK) {
            List<Map<String, Object>> vehicleRecords = queryBridgeSafe(req.getCitizenNationalId(), "VEHICLE_RECORD");
            results.put("VEHICLE_RECORD", vehicleRecords);
        }

        String resultsJson = serializeToJson(results);

        CitizenCheck check = CitizenCheck.builder()
                .citizenNationalId(req.getCitizenNationalId())
                .checkedByOfficerId(officerId)
                .checkType(req.getCheckType())
                .bridgeResults(resultsJson)
                .performedAt(LocalDateTime.now())
                .build();

        CitizenCheck saved = checkRepository.save(check);
        return toResponse(saved, results);
    }

    @Transactional(readOnly = true)
    public CitizenCheckResponse getById(UUID id) {
        CitizenCheck check = checkRepository.findById(id)
                .orElseThrow(() -> new CitizenCheckNotFoundException(id));
        Map<String, Object> results = deserializeFromJson(check.getBridgeResults());
        return toResponse(check, results);
    }

    /** Queries bridge; returns empty list on any error (graceful degradation). */
    private List<Map<String, Object>> queryBridgeSafe(String nationalId, String dataType) {
        try {
            return bridgeQueryService.query(nationalId, dataType);
        } catch (Exception e) {
            log.warn("Bridge query for {}/{} failed gracefully: {}", nationalId, dataType, e.getMessage());
            return List.of();
        }
    }

    private String serializeToJson(Map<String, Object> map) {
        try { return objectMapper.writeValueAsString(map); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeFromJson(String json) {
        if (json == null) return Map.of();
        try { return objectMapper.readValue(json, Map.class); }
        catch (Exception e) { return Map.of(); }
    }

    private CitizenCheckResponse toResponse(CitizenCheck c, Map<String, Object> results) {
        JsonNode node;
        try { node = objectMapper.valueToTree(results); }
        catch (Exception e) { node = objectMapper.createObjectNode(); }

        return CitizenCheckResponse.builder()
                .checkId(c.getCheckId()).citizenNationalId(c.getCitizenNationalId())
                .checkedByOfficerId(c.getCheckedByOfficerId()).checkType(c.getCheckType())
                .bridgeResults(node).performedAt(c.getPerformedAt()).build();
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }
}
