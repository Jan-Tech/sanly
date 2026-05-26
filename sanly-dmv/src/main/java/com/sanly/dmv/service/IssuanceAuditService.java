package com.sanly.dmv.service;

import com.sanly.dmv.entity.IssuanceAuditLog;
import com.sanly.dmv.entity.LicenseCategory;
import com.sanly.dmv.repository.IssuanceAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Saves issuance audit records in a dedicated transaction (REQUIRES_NEW).
 * This ensures audit entries are committed even when the outer issuance
 * transaction rolls back due to a business rule violation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssuanceAuditService {

    private final IssuanceAuditLogRepository auditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(UUID applicationId, String citizenNin, LicenseCategory category,
                           Long officerId, String visionTestRef, String licenseNumber) {
        persist(applicationId, citizenNin, category, officerId,
                "SUCCESS", null, visionTestRef, licenseNumber);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(UUID applicationId, String citizenNin, LicenseCategory category,
                           Long officerId, String outcome, String detail, String visionTestRef) {
        persist(applicationId, citizenNin, category, officerId,
                outcome, detail, visionTestRef, null);
    }

    private void persist(UUID appId, String citizenNin, LicenseCategory category, Long officerId,
                         String outcome, String detail, String visionRef, String licenseNumber) {
        try {
            auditRepository.save(IssuanceAuditLog.builder()
                    .applicationId(appId)
                    .citizenNationalId(citizenNin)
                    .category(category)
                    .processedByOfficerId(officerId)
                    .outcome(outcome)
                    .outcomeDetail(detail)
                    .visionTestRef(visionRef)
                    .licenseNumber(licenseNumber)
                    .build());
        } catch (Exception e) {
            log.error("Failed to save issuance audit log (outcome={}): {}", outcome, e.getMessage());
        }
    }
}
