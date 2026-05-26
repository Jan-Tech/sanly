package com.sanly.civil.client;

import com.sanly.civil.entity.BirthRecord;
import com.sanly.civil.entity.DeathRecord;
import com.sanly.civil.entity.MarriageRecord;
import com.sanly.civil.repository.BirthRecordRepository;
import com.sanly.civil.repository.DeathRecordRepository;
import com.sanly.civil.repository.MarriageRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class BridgePublisherService {
    private static final Logger log = LoggerFactory.getLogger(BridgePublisherService.class);

    private final RestTemplate restTemplate;
    private final BirthRecordRepository birthRecordRepository;
    private final MarriageRecordRepository marriageRecordRepository;
    private final DeathRecordRepository deathRecordRepository;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   BirthRecordRepository birthRecordRepository,
                                   MarriageRecordRepository marriageRecordRepository,
                                   DeathRecordRepository deathRecordRepository,
                                   @Value("${bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${bridge.institution-code}") String institutionCode,
                                   @Value("${bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.birthRecordRepository = birthRecordRepository;
        this.marriageRecordRepository = marriageRecordRepository;
        this.deathRecordRepository = deathRecordRepository;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishBirthRecord(UUID recordId) {
        try {
            BirthRecord record = birthRecordRepository.findById(recordId).orElse(null);
            if (record == null) return;
            publish(record.getChildNationalId(), "BIRTH_RECORD", record.getCertificateNumber(),
                    Map.of("certificateNumber", record.getCertificateNumber(),
                            "childFirstName", record.getChildFirstName(),
                            "childLastName", record.getChildLastName(),
                            "dateOfBirth", record.getDateOfBirth().toString()));
            record.setBridgePublished(true);
            birthRecordRepository.save(record);
        } catch (Exception ex) {
            log.warn("Bridge publish failed for birth record {}: {}", recordId, ex.getMessage());
        }
    }

    @Async("bridgePublishExecutor")
    public void publishMarriageRecord(UUID recordId) {
        try {
            MarriageRecord record = marriageRecordRepository.findById(recordId).orElse(null);
            if (record == null) return;
            publish(record.getSpouse1NationalId(), "MARRIAGE_RECORD", record.getCertificateNumber(),
                    Map.of("certificateNumber", record.getCertificateNumber(),
                            "spouse1NationalId", record.getSpouse1NationalId(),
                            "spouse2NationalId", record.getSpouse2NationalId(),
                            "marriageDate", record.getMarriageDate().toString(),
                            "status", record.getStatus().name()));
            record.setBridgePublished(true);
            marriageRecordRepository.save(record);
        } catch (Exception ex) {
            log.warn("Bridge publish failed for marriage record {}: {}", recordId, ex.getMessage());
        }
    }

    @Async("bridgePublishExecutor")
    public void publishDeathRecord(UUID recordId) {
        try {
            DeathRecord record = deathRecordRepository.findById(recordId).orElse(null);
            if (record == null) return;
            publish(record.getDeceasedNationalId(), "DEATH_RECORD", record.getCertificateNumber(),
                    Map.of("certificateNumber", record.getCertificateNumber(),
                            "deceasedFullName", record.getDeceasedFullName(),
                            "dateOfDeath", record.getDateOfDeath().toString()));
            record.setBridgePublished(true);
            deathRecordRepository.save(record);
        } catch (Exception ex) {
            log.warn("Bridge publish failed for death record {}: {}", recordId, ex.getMessage());
        }
    }

    private void publish(String nationalId, String dataType, String recordRef, Map<String, String> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Institution-Code", institutionCode);
        headers.set("X-Institution-Key", institutionKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "subjectNationalId", nationalId,
                "dataType", dataType,
                "recordRef", recordRef,
                "payload", payload
        );
        restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                new HttpEntity<>(body, headers), Void.class);
        log.info("Published {} for {} to bridge", dataType, nationalId);
    }
}
