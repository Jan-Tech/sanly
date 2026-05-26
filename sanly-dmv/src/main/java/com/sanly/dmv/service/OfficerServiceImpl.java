package com.sanly.dmv.service;

import com.sanly.dmv.dto.request.OfficerCreateRequest;
import com.sanly.dmv.dto.request.OfficerStatusRequest;
import com.sanly.dmv.dto.response.OfficerResponse;
import com.sanly.dmv.entity.DmvOfficer;
import com.sanly.dmv.exception.DuplicateLicenseException;
import com.sanly.dmv.exception.OfficerNotFoundException;
import com.sanly.dmv.repository.OfficerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficerServiceImpl implements OfficerService {

    private final OfficerRepository officerRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    @Transactional
    public OfficerResponse register(OfficerCreateRequest req) {
        if (officerRepository.existsByUsername(req.getUsername()))
            throw new DuplicateLicenseException("Username '" + req.getUsername() + "' is already taken");
        if (StringUtils.hasText(req.getNationalId()) && officerRepository.existsByNationalId(req.getNationalId()))
            throw new DuplicateLicenseException("National ID '" + req.getNationalId() + "' is already registered");

        DmvOfficer officer = DmvOfficer.builder()
                .nationalId(req.getNationalId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .officeRegion(req.getOfficeRegion())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of("ROLE_OFFICER"))
                .build();

        DmvOfficer saved = officerRepository.save(officer);
        log.info("Registered officer {} {} (id={})", saved.getFirstName(), saved.getLastName(), saved.getOfficerId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficerResponse> listAll() {
        return officerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OfficerResponse getById(Long id) { return toResponse(findOrThrow(id)); }

    @Override
    @Transactional
    public OfficerResponse updateStatus(Long id, OfficerStatusRequest req) {
        DmvOfficer officer = findOrThrow(id);
        officer.setStatus(req.getStatus());
        log.info("Officer {} status → {}", id, req.getStatus());
        return toResponse(officerRepository.save(officer));
    }

    private DmvOfficer findOrThrow(Long id) {
        return officerRepository.findById(id).orElseThrow(() -> new OfficerNotFoundException(id));
    }

    private OfficerResponse toResponse(DmvOfficer o) {
        return OfficerResponse.builder()
                .officerId(o.getOfficerId()).nationalId(o.getNationalId())
                .firstName(o.getFirstName()).lastName(o.getLastName())
                .officeRegion(o.getOfficeRegion()).username(o.getUsername())
                .roles(o.getRoles()).status(o.getStatus()).createdAt(o.getCreatedAt())
                .build();
    }
}
