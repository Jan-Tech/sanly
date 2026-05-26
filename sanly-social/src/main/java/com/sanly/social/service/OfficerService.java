package com.sanly.social.service;

import com.sanly.social.dto.request.CreateOfficerRequest;
import com.sanly.social.dto.response.OfficerResponse;
import com.sanly.social.entity.OfficerStatus;
import com.sanly.social.entity.SocialOfficer;
import com.sanly.social.exception.DuplicateResourceException;
import com.sanly.social.exception.RecordNotFoundException;
import com.sanly.social.repository.SocialOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfficerService {

    private final SocialOfficerRepository officerRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (officerRepo.existsByUsername(req.username()))
            throw new DuplicateResourceException("Username already taken: " + req.username());
        if (officerRepo.existsByNationalId(req.nationalId()))
            throw new DuplicateResourceException("Officer already exists for NIN: " + req.nationalId());

        SocialOfficer officer = SocialOfficer.builder()
                .nationalId(req.nationalId())
                .firstName("")
                .lastName("")
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .region(req.region())
                .role(req.role())
                .build();
        return OfficerResponse.from(officerRepo.save(officer));
    }

    public List<OfficerResponse> findAll() {
        return officerRepo.findAll().stream().map(OfficerResponse::from).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(UUID officerId, OfficerStatus status) {
        SocialOfficer officer = officerRepo.findById(officerId)
                .orElseThrow(() -> new RecordNotFoundException("Officer not found: " + officerId));
        officer.setStatus(status);
        return OfficerResponse.from(officerRepo.save(officer));
    }
}
