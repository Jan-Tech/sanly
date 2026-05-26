package com.sanly.dmv.service;

import com.sanly.dmv.client.BridgeQueryService;
import com.sanly.dmv.client.BridgeRecord;
import com.sanly.dmv.client.CitizenRegistryClient;
import com.sanly.dmv.client.NotificationClient;
import com.sanly.dmv.config.UserDetailsImpl;
import com.sanly.dmv.dto.request.IssueLicenseRequest;
import com.sanly.dmv.dto.request.LicenseStatusRequest;
import com.sanly.dmv.dto.response.DrivingLicenseResponse;
import com.sanly.dmv.dto.response.LicenseVerifyResponse;
import com.sanly.dmv.dto.response.PageResponse;
import com.sanly.dmv.entity.*;
import com.sanly.dmv.exception.*;
import com.sanly.dmv.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingLicenseServiceImpl {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DrivingLicenseRepository      licenseRepository;
    private final LicenseApplicationRepository  appRepository;
    private final LicenseSequenceRepository     seqRepository;
    private final CitizenRegistryClient         citizenClient;
    private final BridgeQueryService            bridgeQuery;
    private final IssuanceAuditService          auditService;
    private final NotificationClient            notificationClient;

    // ===== Core issuance logic =====

    /**
     * Issues a driving license.
     *
     * Pre-conditions (all checked, each failure logged before throwing):
     * 1. Application must exist and be APPROVED.
     * 2. Citizen must be ACTIVE in citizen-registry.
     * 3. Citizen must not already have an ACTIVE license for the same category.
     * 4. A VISION_TEST record from SANLY Bridge must exist, be non-expired, and have result PASS.
     *
     * License number format: TM-DL-YYYYNNNNNN (e.g. TM-DL-2026000001)
     */
    @Transactional
    public DrivingLicenseResponse issueLicense(IssueLicenseRequest req) {
        Long officerId = officerIdFromContext();

        // 1. Validate application
        LicenseApplication app = appRepository.findById(req.getApplicationId())
                .orElseThrow(() -> new ApplicationNotFoundException(req.getApplicationId()));

        if (app.getStatus() != ApplicationStatus.APPROVED) {
            auditService.logFailure(app.getApplicationId(), app.getCitizenNationalId(),
                    app.getRequestedCategory(), officerId, "REJECTED_NOT_APPROVED",
                    "Application status is " + app.getStatus(), null);
            throw new ApplicationNotApprovedException(
                    "Application " + req.getApplicationId() + " is not APPROVED (current: " + app.getStatus() + ")");
        }

        // 2. Verify citizen
        citizenClient.verify(app.getCitizenNationalId());

        // 3. Duplicate check
        if (licenseRepository.existsByCitizenNationalIdAndCategoryAndStatus(
                app.getCitizenNationalId(), app.getRequestedCategory(), LicenseStatus.ACTIVE)) {
            auditService.logFailure(app.getApplicationId(), app.getCitizenNationalId(),
                    app.getRequestedCategory(), officerId, "REJECTED_DUPLICATE",
                    "Citizen already has ACTIVE " + app.getRequestedCategory() + " license", null);
            throw new DuplicateLicenseException(
                    "Citizen already has an ACTIVE license for category " + app.getRequestedCategory());
        }

        // 4. Query vision test from SANLY Bridge
        List<BridgeRecord> records = bridgeQuery.queryVisionTests(app.getCitizenNationalId());

        if (records.isEmpty()) {
            auditService.logFailure(app.getApplicationId(), app.getCitizenNationalId(),
                    app.getRequestedCategory(), officerId, "REJECTED_NO_VISION", null, null);
            throw new VisionTestRequiredException();
        }

        // Take the most recent record (bridge returns sorted by publishedAt desc)
        BridgeRecord latest = records.get(0);

        if (latest.isExpired()) {
            String expDate = latest.getExpiresAt().format(DATE_FMT);
            auditService.logFailure(app.getApplicationId(), app.getCitizenNationalId(),
                    app.getRequestedCategory(), officerId, "REJECTED_EXPIRED",
                    "Vision test expired on " + expDate, latest.getRecordRef());
            throw new VisionTestExpiredException(expDate);
        }

        String result = latest.getSummaryResult();
        if (!"PASS".equalsIgnoreCase(result)) {
            auditService.logFailure(app.getApplicationId(), app.getCitizenNationalId(),
                    app.getRequestedCategory(), officerId, "REJECTED_FAIL",
                    "Vision test result: " + result, latest.getRecordRef());
            throw new VisionTestFailedException();
        }

        // 5. All checks passed — issue the license
        LocalDateTime now = LocalDateTime.now();
        String licenseNumber = generateLicenseNumber(now.getYear());

        DrivingLicense license = DrivingLicense.builder()
                .citizenNationalId(app.getCitizenNationalId())
                .licenseNumber(licenseNumber)
                .category(app.getRequestedCategory())
                .issuedAt(now)
                .expiresAt(now.plusYears(4))
                .issuedByOfficerId(officerId)
                .status(LicenseStatus.ACTIVE)
                .visionTestRef(latest.getRecordRef())
                .notes(req.getNotes())
                .build();

        DrivingLicense saved = licenseRepository.save(license);

        auditService.logSuccess(app.getApplicationId(), app.getCitizenNationalId(),
                app.getRequestedCategory(), officerId, latest.getRecordRef(), licenseNumber);

        log.info("Issued license {} for NIN={} category={} officer={}",
                licenseNumber, app.getCitizenNationalId(), app.getRequestedCategory(), officerId);

        notificationClient.send(app.getCitizenNationalId(), "DRIVING_LICENSE_ISSUED", "EN",
                java.util.Map.of(
                        "licenseNumber", licenseNumber,
                        "category",      app.getRequestedCategory().name(),
                        "issuedAt",      saved.getIssuedAt().format(DATE_FMT),
                        "expiresAt",     saved.getExpiresAt().format(DATE_FMT)
                ));

        return toResponse(saved);
    }

    // ===== Read =====

    @Transactional(readOnly = true)
    public DrivingLicenseResponse getById(UUID licenseId) {
        DrivingLicense license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new DrivingLicenseNotFoundException("License not found: " + licenseId));
        return toResponse(license);
    }

    @Transactional(readOnly = true)
    public PageResponse<DrivingLicenseResponse> getByNationalId(String nationalId, int page, int size) {
        Page<DrivingLicense> result = licenseRepository
                .findByCitizenNationalIdOrderByIssuedAtDesc(nationalId,
                        PageRequest.of(page, size, Sort.by("issuedAt").descending()));
        return toPageResponse(result.map(this::toResponse));
    }

    /**
     * Public endpoint — used by police/checkpoints to verify a license.
     * Never throws 404 — returns exists=false instead.
     */
    @Transactional(readOnly = true)
    public LicenseVerifyResponse verify(String licenseNumber) {
        return licenseRepository.findByLicenseNumber(licenseNumber)
                .map(l -> {
                    boolean valid = l.getStatus() == LicenseStatus.ACTIVE
                            && l.getExpiresAt().isAfter(LocalDateTime.now());
                    return LicenseVerifyResponse.builder()
                            .licenseNumber(l.getLicenseNumber())
                            .citizenNationalId(l.getCitizenNationalId())
                            .category(l.getCategory())
                            .issuedAt(l.getIssuedAt())
                            .expiresAt(l.getExpiresAt())
                            .status(l.getStatus())
                            .valid(valid)
                            .build();
                })
                .orElseThrow(() -> new DrivingLicenseNotFoundException(
                        "No license found with number: " + licenseNumber));
    }

    @Transactional
    public DrivingLicenseResponse updateStatus(UUID licenseId, LicenseStatusRequest req) {
        DrivingLicense license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new DrivingLicenseNotFoundException("License not found: " + licenseId));
        license.setStatus(req.getStatus());
        if (StringUtils.hasText(req.getReason())) {
            license.setNotes((license.getNotes() != null ? license.getNotes() + "\n" : "")
                    + "[" + req.getStatus() + "] " + req.getReason());
        }
        log.info("License {} status → {}", licenseId, req.getStatus());
        return toResponse(licenseRepository.save(license));
    }

    // ===== License number generation (pessimistic-locked) =====

    private String generateLicenseNumber(int year) {
        seqRepository.insertIfNotExists(year);
        LicenseSequence seq = seqRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException(
                        "License sequence row missing for year " + year));
        int next = seq.getLastCounter() + 1;
        seq.setLastCounter(next);
        seqRepository.save(seq);
        return "TM-DL-%d%06d".formatted(year, next);
    }

    // ===== Mapping =====

    private DrivingLicenseResponse toResponse(DrivingLicense l) {
        return DrivingLicenseResponse.builder()
                .licenseId(l.getLicenseId())
                .citizenNationalId(l.getCitizenNationalId())
                .licenseNumber(l.getLicenseNumber())
                .category(l.getCategory())
                .issuedAt(l.getIssuedAt())
                .expiresAt(l.getExpiresAt())
                .issuedByOfficerId(l.getIssuedByOfficerId())
                .status(l.getStatus())
                .visionTestRef(l.getVisionTestRef())
                .notes(l.getNotes())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }

    private <T> PageResponse<T> toPageResponse(Page<T> p) {
        return PageResponse.<T>builder()
                .content(p.getContent()).page(p.getNumber()).size(p.getSize())
                .totalElements(p.getTotalElements()).totalPages(p.getTotalPages())
                .last(p.isLast()).build();
    }

    private Long officerIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }
}
