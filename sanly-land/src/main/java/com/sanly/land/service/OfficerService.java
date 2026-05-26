package com.sanly.land.service;

import com.sanly.land.dto.request.CreateOfficerRequest;
import com.sanly.land.dto.response.OfficerResponse;
import com.sanly.land.entity.LandOfficer;
import com.sanly.land.entity.OfficerStatus;
import com.sanly.land.exception.DuplicateResourceException;
import com.sanly.land.exception.RecordNotFoundException;
import com.sanly.land.repository.LandOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfficerService {
    private final LandOfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (officerRepository.existsByUsername(req.username()))
            throw new DuplicateResourceException("Username already exists: " + req.username());
        if (officerRepository.existsByNationalId(req.nationalId()))
            throw new DuplicateResourceException("National ID already registered: " + req.nationalId());
        LandOfficer officer = LandOfficer.builder()
                .nationalId(req.nationalId())
                .firstName(req.firstName())
                .lastName(req.lastName())
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .region(req.region())
                .role(req.role())
                .status(OfficerStatus.ACTIVE)
                .build();
        return OfficerResponse.from(officerRepository.save(officer));
    }

    public List<OfficerResponse> findAll() {
        return officerRepository.findAll().stream().map(OfficerResponse::from).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(UUID id, OfficerStatus status) {
        LandOfficer o = officerRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Officer not found: " + id));
        o.setStatus(status);
        return OfficerResponse.from(officerRepository.save(o));
    }
}
