package com.sanly.pension.service;

import com.sanly.pension.dto.request.CreateOfficerRequest;
import com.sanly.pension.dto.response.OfficerResponse;
import com.sanly.pension.entity.OfficerStatus;
import com.sanly.pension.entity.PensionOfficer;
import com.sanly.pension.exception.BusinessException;
import com.sanly.pension.exception.ResourceNotFoundException;
import com.sanly.pension.repository.PensionOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficerService {
    private final PensionOfficerRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(CreateOfficerRequest req) {
        if (repo.findByUsername(req.getUsername()).isPresent())
            throw new BusinessException("Username already taken: " + req.getUsername());
        return toResponse(repo.save(PensionOfficer.builder()
                .nationalId(req.getNationalId()).firstName(req.getFirstName()).lastName(req.getLastName())
                .username(req.getUsername()).password(passwordEncoder.encode(req.getPassword()))
                .region(req.getRegion()).role(req.getRole()).status(OfficerStatus.ACTIVE)
                .employerCode(req.getEmployerCode()).build()));
    }

    @Transactional(readOnly = true)
    public List<OfficerResponse> findAll() { return repo.findAll().stream().map(this::toResponse).toList(); }

    @Transactional
    public OfficerResponse updateStatus(Long id, OfficerStatus status) {
        PensionOfficer o = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found: " + id));
        o.setStatus(status); return toResponse(repo.save(o));
    }

    private OfficerResponse toResponse(PensionOfficer o) {
        return new OfficerResponse(o.getOfficerId(), o.getNationalId(), o.getFirstName(),
                o.getLastName(), o.getUsername(), o.getRegion(), o.getRole().name(),
                o.getStatus().name(), o.getEmployerCode());
    }
}
