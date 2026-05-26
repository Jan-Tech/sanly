package com.sanly.vehicle.service;

import com.sanly.vehicle.dto.request.CreateOfficerRequest;
import com.sanly.vehicle.dto.response.OfficerResponse;
import com.sanly.vehicle.entity.OfficerStatus;
import com.sanly.vehicle.entity.VehicleOfficer;
import com.sanly.vehicle.exception.BusinessException;
import com.sanly.vehicle.exception.ResourceNotFoundException;
import com.sanly.vehicle.repository.VehicleOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficerService {
    private final VehicleOfficerRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (repo.findByUsername(req.getUsername()).isPresent())
            throw new BusinessException("Username already taken: " + req.getUsername());
        VehicleOfficer officer = repo.save(VehicleOfficer.builder()
                .nationalId(req.getNationalId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .region(req.getRegion())
                .role(req.getRole())
                .status(OfficerStatus.ACTIVE)
                .build());
        return toResponse(officer);
    }

    @Transactional(readOnly = true)
    public List<OfficerResponse> findAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(Long officerId, OfficerStatus status) {
        VehicleOfficer o = repo.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found: " + officerId));
        o.setStatus(status);
        return toResponse(repo.save(o));
    }

    private OfficerResponse toResponse(VehicleOfficer o) {
        return new OfficerResponse(o.getOfficerId(), o.getNationalId(), o.getFirstName(),
                o.getLastName(), o.getUsername(), o.getRegion(), o.getRole().name(), o.getStatus().name());
    }
}
