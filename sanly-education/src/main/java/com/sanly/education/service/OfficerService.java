package com.sanly.education.service;

import com.sanly.education.dto.request.CreateOfficerRequest;
import com.sanly.education.dto.response.OfficerResponse;
import com.sanly.education.entity.EducationOfficer;
import com.sanly.education.entity.OfficerStatus;
import com.sanly.education.exception.DuplicateResourceException;
import com.sanly.education.exception.RecordNotFoundException;
import com.sanly.education.repository.EducationOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfficerService {
    private final EducationOfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (officerRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already exists: " + req.username());
        }
        if (officerRepository.existsByNationalId(req.nationalId())) {
            throw new DuplicateResourceException("National ID already registered: " + req.nationalId());
        }
        EducationOfficer officer = EducationOfficer.builder()
                .nationalId(req.nationalId())
                .firstName(req.firstName())
                .lastName(req.lastName())
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .institutionCode(req.institutionCode())
                .role(req.role())
                .status(OfficerStatus.ACTIVE)
                .build();
        return OfficerResponse.from(officerRepository.save(officer));
    }

    public List<OfficerResponse> findAll() {
        return officerRepository.findAll().stream().map(OfficerResponse::from).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(UUID officerId, OfficerStatus status) {
        EducationOfficer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new RecordNotFoundException("Officer not found: " + officerId));
        officer.setStatus(status);
        return OfficerResponse.from(officerRepository.save(officer));
    }
}
