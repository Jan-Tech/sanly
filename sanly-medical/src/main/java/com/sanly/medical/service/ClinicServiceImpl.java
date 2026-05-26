package com.sanly.medical.service;

import com.sanly.medical.dto.request.ClinicCreateRequest;
import com.sanly.medical.dto.request.ClinicStatusRequest;
import com.sanly.medical.dto.response.ClinicResponse;
import com.sanly.medical.entity.Clinic;
import com.sanly.medical.exception.ClinicNotFoundException;
import com.sanly.medical.exception.DuplicateLicenseException;
import com.sanly.medical.repository.ClinicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;

    @Override
    @Transactional
    public ClinicResponse register(ClinicCreateRequest req) {
        if (clinicRepository.existsByLicenseNumber(req.getLicenseNumber())) {
            throw new DuplicateLicenseException("Clinic", req.getLicenseNumber());
        }
        Clinic clinic = Clinic.builder()
                .name(req.getName())
                .licenseNumber(req.getLicenseNumber())
                .region(req.getRegion())
                .address(req.getAddress())
                .phone(req.getPhone())
                .build();
        Clinic saved = clinicRepository.save(clinic);
        log.info("Registered clinic: {} (id={})", saved.getName(), saved.getClinicId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicResponse> listAll() {
        return clinicRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicResponse getById(Long clinicId) {
        return toResponse(findOrThrow(clinicId));
    }

    @Override
    @Transactional
    public ClinicResponse updateStatus(Long clinicId, ClinicStatusRequest req) {
        Clinic clinic = findOrThrow(clinicId);
        clinic.setStatus(req.getStatus());
        log.info("Clinic {} status changed to {}", clinicId, req.getStatus());
        return toResponse(clinicRepository.save(clinic));
    }

    private Clinic findOrThrow(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ClinicNotFoundException(id));
    }

    private ClinicResponse toResponse(Clinic c) {
        return ClinicResponse.builder()
                .clinicId(c.getClinicId())
                .name(c.getName())
                .licenseNumber(c.getLicenseNumber())
                .region(c.getRegion())
                .address(c.getAddress())
                .phone(c.getPhone())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
