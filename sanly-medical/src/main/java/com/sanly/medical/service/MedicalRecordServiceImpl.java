package com.sanly.medical.service;

import com.sanly.medical.client.BridgePublisherService;
import com.sanly.medical.client.CitizenRegistryClient;
import com.sanly.medical.config.UserDetailsImpl;
import com.sanly.medical.dto.request.MedicalRecordCreateRequest;
import com.sanly.medical.dto.request.RecordResultUpdateRequest;
import com.sanly.medical.dto.response.MedicalRecordResponse;
import com.sanly.medical.dto.response.PageResponse;
import com.sanly.medical.entity.Clinic;
import com.sanly.medical.entity.ClinicStatus;
import com.sanly.medical.entity.MedicalRecord;
import com.sanly.medical.exception.ClinicAccessDeniedException;
import com.sanly.medical.exception.ClinicNotFoundException;
import com.sanly.medical.exception.ClinicSuspendedException;
import com.sanly.medical.exception.MedicalRecordNotFoundException;
import com.sanly.medical.repository.ClinicRepository;
import com.sanly.medical.repository.MedicalRecordRepository;
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

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final ClinicRepository        clinicRepository;
    private final CitizenRegistryClient   citizenRegistryClient;
    private final BridgePublisherService  bridgePublisher;

    @Override
    @Transactional
    public MedicalRecordResponse create(MedicalRecordCreateRequest req) {
        UserDetailsImpl principal = authenticatedPrincipal();

        // Verify citizen exists and is ACTIVE in citizen-registry (external call)
        citizenRegistryClient.verify(req.getCitizenNationalId());

        // Ensure doctor's clinic is active
        Long clinicId = principal.getClinicId();
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ClinicNotFoundException(clinicId));
        if (clinic.getStatus() == ClinicStatus.SUSPENDED) {
            throw new ClinicSuspendedException(clinicId);
        }

        MedicalRecord record = MedicalRecord.builder()
                .citizenNationalId(req.getCitizenNationalId())
                .doctorId(principal.getDoctorId())
                .clinicId(clinicId)
                .testType(req.getTestType())
                .notes(req.getNotes())
                .testedAt(req.getTestedAt())
                .expiresAt(req.getExpiresAt())
                .build();

        MedicalRecord saved = recordRepository.save(record);
        log.info("Created medical record {} ({}) for NIN={}",
                saved.getRecordId(), saved.getTestType(), saved.getCitizenNationalId());

        // Async: publish to SANLY Bridge — never blocks the doctor's response
        bridgePublisher.publish(saved);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getById(UUID recordId) {
        UserDetailsImpl principal = authenticatedPrincipal();

        MedicalRecord record = principal.isAdmin()
                ? recordRepository.findById(recordId)
                    .orElseThrow(() -> new MedicalRecordNotFoundException(recordId))
                : recordRepository.findByRecordIdAndClinicId(recordId, principal.getClinicId())
                    .orElseThrow(() -> new MedicalRecordNotFoundException(recordId));

        return toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordResponse> getByNationalId(String nationalId, int page, int size) {
        UserDetailsImpl principal = authenticatedPrincipal();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("testedAt").descending());

        Page<MedicalRecord> result = principal.isAdmin()
                ? recordRepository.findByCitizenNationalIdOrderByTestedAtDesc(nationalId, pageable)
                : recordRepository.findByCitizenNationalIdAndClinicIdOrderByTestedAtDesc(
                        nationalId, principal.getClinicId(), pageable);

        return toPageResponse(result.map(this::toResponse));
    }

    @Override
    @Transactional
    public MedicalRecordResponse updateResult(UUID recordId, RecordResultUpdateRequest req) {
        UserDetailsImpl principal = authenticatedPrincipal();

        MedicalRecord record = principal.isAdmin()
                ? recordRepository.findById(recordId)
                    .orElseThrow(() -> new MedicalRecordNotFoundException(recordId))
                : recordRepository.findByRecordIdAndClinicId(recordId, principal.getClinicId())
                    .orElseThrow(() -> new MedicalRecordNotFoundException(recordId));

        record.setResult(req.getResult());
        if (StringUtils.hasText(req.getNotes())) {
            record.setNotes(req.getNotes());
        }
        // Reset bridge publish flag so the updated record is re-published
        record.setBridgePublished(false);
        record.setBridgePublishedAt(null);

        MedicalRecord saved = recordRepository.save(record);
        log.info("Updated result for record {} → {}", recordId, req.getResult());

        // Re-publish updated record to SANLY Bridge
        bridgePublisher.publish(saved);

        return toResponse(saved);
    }

    // ---- helpers ----

    private UserDetailsImpl authenticatedPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) auth.getPrincipal();
    }

    private MedicalRecordResponse toResponse(MedicalRecord r) {
        return MedicalRecordResponse.builder()
                .recordId(r.getRecordId())
                .citizenNationalId(r.getCitizenNationalId())
                .doctorId(r.getDoctorId())
                .clinicId(r.getClinicId())
                .testType(r.getTestType())
                .result(r.getResult())
                .notes(r.getNotes())
                .testedAt(r.getTestedAt())
                .expiresAt(r.getExpiresAt())
                .bridgePublished(r.isBridgePublished())
                .bridgePublishedAt(r.getBridgePublishedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private <T> PageResponse<T> toPageResponse(Page<T> p) {
        return PageResponse.<T>builder()
                .content(p.getContent())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }
}
