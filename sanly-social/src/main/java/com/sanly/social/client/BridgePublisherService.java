package com.sanly.social.client;

import com.sanly.social.entity.BenefitClaim;
import com.sanly.social.entity.ClaimStatus;
import com.sanly.social.repository.BenefitClaimRepository;
import com.sanly.social.repository.BenefitProgramRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BridgePublisherService {
    private static final Logger log = LoggerFactory.getLogger(BridgePublisherService.class);

    private final RestTemplate restTemplate;
    private final BenefitClaimRepository claimRepository;
    private final BenefitProgramRepository programRepository;
    private final String bridgeBaseUrl;
    private final String institutionCode;
    private final String institutionKey;

    public BridgePublisherService(RestTemplate restTemplate,
                                   BenefitClaimRepository claimRepository,
                                   BenefitProgramRepository programRepository,
                                   @Value("${sanly.bridge.base-url}") String bridgeBaseUrl,
                                   @Value("${sanly.bridge.institution-code}") String institutionCode,
                                   @Value("${sanly.bridge.institution-key}") String institutionKey) {
        this.restTemplate = restTemplate;
        this.claimRepository = claimRepository;
        this.programRepository = programRepository;
        this.bridgeBaseUrl = bridgeBaseUrl;
        this.institutionCode = institutionCode;
        this.institutionKey = institutionKey;
    }

    @Async("bridgePublishExecutor")
    public void publishBenefitStatus(String citizenNationalId) {
        try {
            List<BenefitClaim> active = claimRepository
                    .findByCitizenNationalIdAndStatus(citizenNationalId, ClaimStatus.ACTIVE);

            List<String> activeTypes = active.stream()
                    .map(c -> programRepository.findByProgramCode(c.getProgramCode())
                            .map(p -> p.getBenefitType().name()).orElse("UNKNOWN"))
                    .distinct().collect(Collectors.toList());

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "subjectNationalId", citizenNationalId,
                    "dataType", "SOCIAL_BENEFIT_STATUS",
                    "recordRef", citizenNationalId + "-benefits",
                    "payload", Map.of(
                            "citizenNationalId",  citizenNationalId,
                            "activeBenefitTypes", activeTypes,
                            "activeClaimCount",   active.size(),
                            "status",             active.isEmpty() ? "NO_ACTIVE_BENEFITS" : "HAS_ACTIVE_BENEFITS"
                    )
            );

            restTemplate.postForEntity(bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers), Void.class);
            log.info("Published SOCIAL_BENEFIT_STATUS for {} to bridge", citizenNationalId);
        } catch (Exception ex) {
            log.warn("Bridge publish failed for {}: {}", citizenNationalId, ex.getMessage());
        }
    }
}
