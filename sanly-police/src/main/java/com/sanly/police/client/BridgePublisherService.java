package com.sanly.police.client;

import com.sanly.police.entity.CriminalRecord;
import com.sanly.police.repository.CriminalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publishes CRIMINAL_RECORD records to SANLY Bridge asynchronously.
 * Never throws — logs errors only (eventual consistency).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BridgePublisherService {

    private final RestTemplate restTemplate;
    private final CriminalRecordRepository recordRepository;

    @Value("${sanly.bridge.base-url}") private String baseUrl;
    @Value("${sanly.bridge.institution-code}") private String institutionCode;
    @Value("${sanly.bridge.institution-key}") private String institutionKey;

    @Async("bridgePublishExecutor")
    public void publishCriminalRecord(CriminalRecord record) {
        try {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("offenseType", record.getOffenseType().name());
            summary.put("verdict", record.getVerdict().name());
            summary.put("courtName", record.getCourtName());
            summary.put("offenseDate", record.getOffenseDate() != null ? record.getOffenseDate().toString() : null);
            summary.put("status", record.getStatus().name());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("nationalId", record.getCitizenNationalId());
            body.put("dataType", "CRIMINAL_RECORD");
            body.put("recordRef", record.getRecordId().toString());
            body.put("summary", summary);
            body.put("expiresAt", record.getExpiresAt() != null ? record.getExpiresAt().toString() : null);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.postForEntity(baseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Object.class);

            record.setBridgePublished(true);
            recordRepository.save(record);
            log.info("Published CRIMINAL_RECORD {} to bridge", record.getRecordId());
        } catch (Exception e) {
            log.error("Failed to publish CRIMINAL_RECORD {} to bridge: {}",
                    record.getRecordId(), e.getMessage());
        }
    }
}
