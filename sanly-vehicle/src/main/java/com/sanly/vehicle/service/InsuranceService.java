package com.sanly.vehicle.service;

import com.sanly.vehicle.config.UserDetailsImpl;
import com.sanly.vehicle.dto.request.RegisterInsuranceRequest;
import com.sanly.vehicle.dto.response.InsuranceResponse;
import com.sanly.vehicle.dto.response.InsuranceVerifyResponse;
import com.sanly.vehicle.entity.InsuranceRecord;
import com.sanly.vehicle.entity.InsuranceStatus;
import com.sanly.vehicle.exception.ResourceNotFoundException;
import com.sanly.vehicle.repository.InsuranceRecordRepository;
import com.sanly.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceService {
    private final InsuranceRecordRepository insuranceRepo;
    private final VehicleRepository vehicleRepo;

    @Transactional
    public InsuranceResponse register(RegisterInsuranceRequest req) {
        vehicleRepo.findByPlateNumber(req.getPlateNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + req.getPlateNumber()));

        InsuranceRecord rec = insuranceRepo.save(InsuranceRecord.builder()
                .plateNumber(req.getPlateNumber())
                .insuranceCompany(req.getInsuranceCompany())
                .policyNumber(req.getPolicyNumber())
                .coverageType(com.sanly.vehicle.entity.CoverageType.valueOf(req.getCoverageType().toString()))
                .validFrom(LocalDate.parse(req.getValidFrom()))
                .validUntil(LocalDate.parse(req.getValidUntil()))
                .status(InsuranceStatus.ACTIVE)
                .registeredByOfficerId(authenticatedOfficerId())
                .build());
        return toResponse(rec);
    }

    @Transactional(readOnly = true)
    public List<InsuranceResponse> findByPlate(String plateNumber) {
        return insuranceRepo.findByPlateNumberOrderByValidFromDesc(plateNumber)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InsuranceVerifyResponse verify(String plateNumber) {
        InsuranceRecord active = insuranceRepo.findByPlateNumberAndStatus(plateNumber, InsuranceStatus.ACTIVE).orElse(null);
        if (active == null) return new InsuranceVerifyResponse(plateNumber, false, null, null, null);
        return new InsuranceVerifyResponse(plateNumber, true, active.getInsuranceCompany(),
                active.getCoverageType().name(), active.getValidUntil());
    }

    @Transactional
    public InsuranceResponse cancel(Long insuranceId) {
        InsuranceRecord rec = insuranceRepo.findById(insuranceId)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance record not found: " + insuranceId));
        rec.setStatus(InsuranceStatus.CANCELLED);
        return toResponse(insuranceRepo.save(rec));
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl ud)) return null;
        return ud.getOfficerId();
    }

    private InsuranceResponse toResponse(InsuranceRecord r) {
        return new InsuranceResponse(r.getInsuranceId(), r.getPlateNumber(), r.getInsuranceCompany(),
                r.getCoverageType().name(), r.getValidFrom(), r.getValidUntil(),
                r.getStatus().name(), r.getRegisteredByOfficerId());
    }
}
