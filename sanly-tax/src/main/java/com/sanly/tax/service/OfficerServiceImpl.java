package com.sanly.tax.service;

import com.sanly.tax.dto.request.OfficerCreateRequest;
import com.sanly.tax.dto.request.OfficerStatusRequest;
import com.sanly.tax.dto.response.OfficerResponse;
import com.sanly.tax.entity.TaxOfficer;
import com.sanly.tax.exception.OfficerNotFoundException;
import com.sanly.tax.repository.OfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service @RequiredArgsConstructor
public class OfficerServiceImpl {
    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(OfficerCreateRequest req) {
        return toResponse(officerRepository.save(TaxOfficer.builder()
                .firstName(req.getFirstName()).lastName(req.getLastName())
                .officeRegion(req.getOfficeRegion()).username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of("ROLE_OFFICER")).build()));
    }

    @Transactional(readOnly = true)
    public List<OfficerResponse> listAll() {
        return officerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(Long id, OfficerStatusRequest req) {
        TaxOfficer o = officerRepository.findById(id).orElseThrow(() -> new OfficerNotFoundException(id));
        o.setStatus(req.getStatus());
        return toResponse(officerRepository.save(o));
    }

    private OfficerResponse toResponse(TaxOfficer o) {
        return OfficerResponse.builder().officerId(o.getOfficerId()).nationalId(o.getNationalId())
                .firstName(o.getFirstName()).lastName(o.getLastName())
                .officeRegion(o.getOfficeRegion()).status(o.getStatus())
                .createdAt(o.getCreatedAt()).build();
    }
}
