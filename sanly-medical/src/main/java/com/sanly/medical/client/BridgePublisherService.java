package com.sanly.medical.client;

import com.sanly.medical.entity.MedicalRecord;
import com.sanly.medical.entity.TestType;
import com.sanly.medical.repository.MedicalRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Publishes medical records to SANLY Bridge asynchronously.
 *
 * Every CREATE and RESULT-UPDATE triggers an async publish so that
 * other institutions (e.g. DMV querying VISION_TEST) see up-to-date data.
 *
 * Design contract:
 * - Never throws — bridge failures are logged and the local save is unaffected.
 * - Marks {@code bridgePublished = true} on success so the record is not re-published.
 * - The {@code findUnpublished()} repository query enables a future scheduled retry job.
 */
@Slf4j
@Service
public class BridgePublisherService {

    private static final String PUBLISH_PATH = "/api/v1/exchange/publish";

    private final RestTemplate             restTemplate;
    private final MedicalRecordRepository  recordRepository;
    private final String                   bridgeBaseUrl;
    private final String                   institutionCode;
    private final String                   institutionKey;

    public BridgePublisherService(
            RestTemplate restTemplate,
            MedicalRecordRepository recordRepository,
            @Value("${sanly.bridge.base-url}")          String bridgeBaseUrl,
            @Value("${sanly.bridge.institution-code}")  String institutionCode,
            @Value("${sanly.bridge.institution-key}")   String institutionKey) {
        this.restTemplate      = restTemplate;
        this.recordRepository  = recordRepository;
        this.bridgeBaseUrl     = bridgeBaseUrl;
        this.institutionCode   = institutionCode;
        this.institutionKey    = institutionKey;
    }

    /**
     * Publish a medical record to SANLY Bridge.
     * Runs in the {@code bridgePublishExecutor} thread pool — does NOT block the caller.
     *
     * @param record the saved MedicalRecord to publish
     */
    @Async("bridgePublishExecutor")
    @Transactional
    public void publish(MedicalRecord record) {
        try {
            Map<String, Object> body = buildPayload(record);
            HttpHeaders headers = institutionHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(bridgeBaseUrl + PUBLISH_PATH, entity, Object.class);

            // Mark published — reload to get managed entity within this transaction
            recordRepository.findById(record.getRecordId()).ifPresent(r -> {
                r.setBridgePublished(true);
                r.setBridgePublishedAt(LocalDateTime.now());
                recordRepository.save(r);
            });

            log.info("Published record {} ({}) to SANLY Bridge",
                    record.getRecordId(), record.getTestType());

        } catch (RestClientException e) {
            log.error("Bridge publish failed for record {} ({}): {}",
                    record.getRecordId(), record.getTestType(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error publishing record {}: {}",
                    record.getRecordId(), e.getMessage(), e);
        }
    }

    // ---- private helpers ----

    private Map<String, Object> buildPayload(MedicalRecord record) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("testType",   record.getTestType().name());
        summary.put("result",     record.getResult().name());
        summary.put("testedAt",   record.getTestedAt().toString());
        summary.put("clinicId",   record.getClinicId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("nationalId", record.getCitizenNationalId());
        payload.put("dataType",   toBridgeDataType(record.getTestType()));
        payload.put("recordRef",  record.getRecordId().toString());
        payload.put("summary",    summary);
        if (record.getExpiresAt() != null) {
            payload.put("expiresAt", record.getExpiresAt().toString());
        }
        return payload;
    }

    private HttpHeaders institutionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Institution-Code", institutionCode);
        headers.set("X-Institution-Key",  institutionKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Maps a local TestType to the SANLY Bridge DataType enum name.
     * VISION_TEST maps 1-to-1; all other medical tests map to MEDICAL_CLEARANCE.
     */
    private String toBridgeDataType(TestType testType) {
        return testType == TestType.VISION_TEST ? "VISION_TEST" : "MEDICAL_CLEARANCE";
    }
}
