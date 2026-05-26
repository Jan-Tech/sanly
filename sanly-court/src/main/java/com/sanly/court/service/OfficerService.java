package com.sanly.court.service;

import com.sanly.court.dto.request.CreateOfficerRequest;
import com.sanly.court.dto.response.OfficerResponse;
import com.sanly.court.entity.CourtOfficer;
import com.sanly.court.entity.OfficerStatus;
import com.sanly.court.exception.DuplicateResourceException;
import com.sanly.court.exception.RecordNotFoundException;
import com.sanly.court.repository.CourtOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfficerService {
    private final CourtOfficerRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (repo.existsByUsername(req.username()))
            throw new DuplicateResourceException("Username taken: " + req.username());
        if (repo.existsByNationalId(req.nationalId()))
            throw new DuplicateResourceException("Officer exists for NIN: " + req.nationalId());
        return OfficerResponse.from(repo.save(CourtOfficer.builder()
                .nationalId(req.nationalId()).firstName(req.firstName()).lastName(req.lastName())
                .username(req.username()).password(passwordEncoder.encode(req.password()))
                .courtCode(req.courtCode()).role(req.role()).build()));
    }

    public List<OfficerResponse> findAll() {
        return repo.findAll().stream().map(OfficerResponse::from).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(UUID officerId, OfficerStatus status) {
        CourtOfficer o = repo.findById(officerId)
                .orElseThrow(() -> new RecordNotFoundException("Officer not found: " + officerId));
        o.setStatus(status); return OfficerResponse.from(repo.save(o));
    }
}
