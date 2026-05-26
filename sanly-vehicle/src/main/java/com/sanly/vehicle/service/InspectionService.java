package com.sanly.vehicle.service;

import com.sanly.vehicle.config.UserDetailsImpl;
import com.sanly.vehicle.dto.request.RecordInspectionRequest;
import com.sanly.vehicle.dto.response.InspectionResponse;
import com.sanly.vehicle.entity.TechnicalInspection;
import com.sanly.vehicle.exception.ResourceNotFoundException;
import com.sanly.vehicle.repository.TechnicalInspectionRepository;
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
public class InspectionService {
    private final TechnicalInspectionRepository inspectionRepo;
    private final VehicleRepository vehicleRepo;

    @Transactional
    public InspectionResponse record(RecordInspectionRequest req) {
        vehicleRepo.findByPlateNumber(req.getPlateNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + req.getPlateNumber()));

        TechnicalInspection insp = inspectionRepo.save(TechnicalInspection.builder()
                .plateNumber(req.getPlateNumber())
                .inspectionDate(LocalDate.parse(req.getInspectionDate()))
                .nextInspectionDue(LocalDate.parse(req.getNextInspectionDue()))
                .result(req.getResult())
                .findings(req.getFindings())
                .inspectedByOfficerId(authenticatedOfficerId())
                .build());
        return toResponse(insp);
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> findByPlate(String plateNumber) {
        return inspectionRepo.findByPlateNumberOrderByInspectionDateDesc(plateNumber)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> findDueWithin30Days() {
        LocalDate today = LocalDate.now();
        return inspectionRepo.findDueBetween(today, today.plusDays(30))
                .stream().map(this::toResponse).toList();
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl ud)) return null;
        return ud.getOfficerId();
    }

    private InspectionResponse toResponse(TechnicalInspection i) {
        return new InspectionResponse(i.getInspectionId(), i.getPlateNumber(),
                i.getInspectionDate(), i.getNextInspectionDue(), i.getResult().name(), i.getInspectedByOfficerId());
    }
}
