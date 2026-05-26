package com.sanly.customs.service;

import com.sanly.customs.dto.request.CreateOfficerRequest;
import com.sanly.customs.dto.response.OfficerResponse;
import com.sanly.customs.entity.CustomsOfficer;
import com.sanly.customs.entity.OfficerStatus;
import com.sanly.customs.exception.DuplicateResourceException;
import com.sanly.customs.exception.RecordNotFoundException;
import com.sanly.customs.repository.CustomsOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfficerService {
    private final CustomsOfficerRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (repo.existsByUsername(req.username()))
            throw new DuplicateResourceException("Username already taken: " + req.username());
        if (repo.existsByNationalId(req.nationalId()))
            throw new DuplicateResourceException("Officer already exists for NIN: " + req.nationalId());
        return OfficerResponse.from(repo.save(CustomsOfficer.builder()
                .nationalId(req.nationalId()).firstName(req.firstName()).lastName(req.lastName())
                .username(req.username()).password(passwordEncoder.encode(req.password()))
                .port(req.port()).role(req.role()).build()));
    }

    public List<OfficerResponse> findAll() {
        return repo.findAll().stream().map(OfficerResponse::from).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(UUID officerId, OfficerStatus status) {
        CustomsOfficer o = repo.findById(officerId)
                .orElseThrow(() -> new RecordNotFoundException("Officer not found: " + officerId));
        o.setStatus(status); return OfficerResponse.from(repo.save(o));
    }
}
