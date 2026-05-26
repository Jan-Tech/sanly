package com.sanly.dmv.service;

import com.sanly.dmv.client.CitizenRegistryClient;
import com.sanly.dmv.config.UserDetailsImpl;
import com.sanly.dmv.dto.request.LicenseApplicationRequest;
import com.sanly.dmv.dto.request.ProcessApplicationRequest;
import com.sanly.dmv.dto.response.LicenseApplicationResponse;
import com.sanly.dmv.dto.response.PageResponse;
import com.sanly.dmv.entity.ApplicationStatus;
import com.sanly.dmv.entity.LicenseApplication;
import com.sanly.dmv.exception.ApplicationNotFoundException;
import com.sanly.dmv.repository.LicenseApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseApplicationServiceImpl {

    private final LicenseApplicationRepository appRepository;
    private final CitizenRegistryClient        citizenClient;

    @Transactional
    public LicenseApplicationResponse create(LicenseApplicationRequest req) {
        citizenClient.verify(req.getCitizenNationalId());

        LicenseApplication app = LicenseApplication.builder()
                .citizenNationalId(req.getCitizenNationalId())
                .requestedCategory(req.getRequestedCategory())
                .build();

        LicenseApplication saved = appRepository.save(app);
        log.info("Created application {} for NIN={} category={}",
                saved.getApplicationId(), saved.getCitizenNationalId(), saved.getRequestedCategory());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LicenseApplicationResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<LicenseApplicationResponse> getByNationalId(String nationalId, int page, int size) {
        Page<LicenseApplication> result = appRepository
                .findByCitizenNationalIdOrderByAppliedAtDesc(nationalId,
                        PageRequest.of(page, size, Sort.by("appliedAt").descending()));
        return toPageResponse(result.map(this::toResponse));
    }

    @Transactional
    public LicenseApplicationResponse process(UUID applicationId, ProcessApplicationRequest req) {
        LicenseApplication app = findOrThrow(applicationId);

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new com.sanly.dmv.exception.ApplicationNotApprovedException(
                    "Application " + applicationId + " is already " + app.getStatus());
        }
        if (req.getStatus() == ApplicationStatus.REJECTED && (req.getRejectionReason() == null
                || req.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting an application");
        }

        Long officerId = officerIdFromContext();
        app.setStatus(req.getStatus());
        app.setRejectionReason(req.getRejectionReason());
        app.setProcessedByOfficerId(officerId);
        app.setProcessedAt(LocalDateTime.now());

        LicenseApplication saved = appRepository.save(app);
        log.info("Application {} → {} by officer {}", applicationId, req.getStatus(), officerId);
        return toResponse(saved);
    }

    // ---- helpers ----

    private LicenseApplication findOrThrow(UUID id) {
        return appRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private Long officerIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }

    private LicenseApplicationResponse toResponse(LicenseApplication a) {
        return LicenseApplicationResponse.builder()
                .applicationId(a.getApplicationId())
                .citizenNationalId(a.getCitizenNationalId())
                .requestedCategory(a.getRequestedCategory())
                .appliedAt(a.getAppliedAt())
                .status(a.getStatus())
                .rejectionReason(a.getRejectionReason())
                .processedByOfficerId(a.getProcessedByOfficerId())
                .processedAt(a.getProcessedAt())
                .build();
    }

    private <T> PageResponse<T> toPageResponse(Page<T> p) {
        return PageResponse.<T>builder()
                .content(p.getContent()).page(p.getNumber()).size(p.getSize())
                .totalElements(p.getTotalElements()).totalPages(p.getTotalPages())
                .last(p.isLast()).build();
    }
}
