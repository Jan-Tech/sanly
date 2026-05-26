package com.sanly.business.service;

import com.sanly.business.dto.CreateOfficerRequest;
import com.sanly.business.dto.OfficerResponse;
import com.sanly.business.entity.OfficerStatus;
import com.sanly.business.entity.RegistrationOfficer;
import com.sanly.business.exception.DuplicateResourceException;
import com.sanly.business.exception.OfficerNotFoundException;
import com.sanly.business.repository.OfficerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class OfficerServiceImpl {
    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficerServiceImpl(OfficerRepository officerRepository, PasswordEncoder passwordEncoder) {
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (officerRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already taken: " + req.username());
        }
        RegistrationOfficer officer = new RegistrationOfficer();
        officer.setUsername(req.username());
        officer.setPassword(passwordEncoder.encode(req.password()));
        officer.setFullName(req.fullName());
        officer.setNationalId(req.nationalId());
        officer.setOfficeRegion(req.officeRegion());
        officer.setStatus(OfficerStatus.ACTIVE);
        officer.setRoles(Set.of("ROLE_OFFICER"));
        return OfficerResponse.from(officerRepository.save(officer));
    }

    public Page<OfficerResponse> findAll(Pageable pageable) {
        return officerRepository.findAll(pageable).map(OfficerResponse::from);
    }

    public OfficerResponse findById(Long id) {
        return OfficerResponse.from(officerRepository.findById(id)
                .orElseThrow(() -> new OfficerNotFoundException("Officer not found: " + id)));
    }

    @Transactional
    public OfficerResponse suspend(Long id) {
        RegistrationOfficer officer = officerRepository.findById(id)
                .orElseThrow(() -> new OfficerNotFoundException("Officer not found: " + id));
        officer.setStatus(OfficerStatus.SUSPENDED);
        return OfficerResponse.from(officerRepository.save(officer));
    }

    @Transactional
    public OfficerResponse activate(Long id) {
        RegistrationOfficer officer = officerRepository.findById(id)
                .orElseThrow(() -> new OfficerNotFoundException("Officer not found: " + id));
        officer.setStatus(OfficerStatus.ACTIVE);
        return OfficerResponse.from(officerRepository.save(officer));
    }
}
