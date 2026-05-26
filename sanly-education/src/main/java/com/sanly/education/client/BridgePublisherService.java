package com.sanly.education.client;

import com.sanly.education.entity.Diploma;
import com.sanly.education.repository.DiplomaRepository;
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
    private final DiplomaRepository diplomaRepository;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   DiplomaRepository diplomaRepository,
                                   @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${sanly.bridge.institution-code}") String institutionCode,
                                   @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.diplomaRepository = diplomaRepository;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishDiploma(UUID diplomaId, String institutionName) {
        try {
            Diploma d = diplomaRepository.findById(diplomaId).orElse(null);
            if (d == null) return;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "subjectNationalId", d.getCitizenNationalId(),
                    "dataType", "EDUCATION_DIPLOMA",
                    "recordRef", d.getDiplomaCode(),
                    "payload", Map.of(
                            "diplomaCode",      d.getDiplomaCode(),
                            "institutionName",  institutionName,
                            "programName",      d.getProgramName(),
                            "programLevel",     d.getProgramLevel().name(),
                            "graduationDate",   d.getGraduationDate().toString(),
                            "honors",           d.getHonors().name(),
                            "status",           d.getStatus().name()
                    )
            );

            restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Void.class);

            d.setBridgePublished(true);
            diplomaRepository.save(d);
            log.info("Published EDUCATION_DIPLOMA {} to bridge", d.getDiplomaCode());
        } catch (Exception ex) {
            log.warn("Bridge publish failed for diploma {}: {}", diplomaId, ex.getMessage());
        }
    }
}
