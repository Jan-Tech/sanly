package com.sanly.pension.service;

import com.sanly.pension.client.BridgeQueryService;
import com.sanly.pension.dto.request.RegisterEmployerRequest;
import com.sanly.pension.dto.response.EmployerResponse;
import com.sanly.pension.entity.Employer;
import com.sanly.pension.entity.EmployerStatus;
import com.sanly.pension.exception.BusinessException;
import com.sanly.pension.exception.ResourceNotFoundException;
import com.sanly.pension.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerService {
    private final EmployerRepository repo;
    private final CodeGeneratorService codeGen;
    private final BridgeQueryService bridgeQueryService;

    @Transactional
    public EmployerResponse register(RegisterEmployerRequest req) {
        if (repo.findByBusinessRegistrationNumber(req.getBusinessRegistrationNumber()).isPresent())
            throw new BusinessException("Employer already registered: " + req.getBusinessRegistrationNumber());
        if (!bridgeQueryService.isBusinessActive(req.getBusinessRegistrationNumber()))
            throw new BusinessException("Business not found or inactive in bridge: " + req.getBusinessRegistrationNumber());

        String code = codeGen.nextEmployerCode();
        return toResponse(repo.save(Employer.builder()
                .employerCode(code).businessName(req.getBusinessName())
                .businessRegistrationNumber(req.getBusinessRegistrationNumber())
                .contactNationalId(req.getContactNationalId())
                .registeredAt(LocalDateTime.now()).status(EmployerStatus.ACTIVE).build()));
    }

    @Transactional(readOnly = true)
    public List<EmployerResponse> findAll() { return repo.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public EmployerResponse findByCode(String code) {
        return toResponse(repo.findByEmployerCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + code)));
    }

    @Transactional
    public EmployerResponse updateStatus(String code, EmployerStatus status) {
        Employer e = repo.findByEmployerCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + code));
        e.setStatus(status); return toResponse(repo.save(e));
    }

    private EmployerResponse toResponse(Employer e) {
        return new EmployerResponse(e.getEmployerId(), e.getEmployerCode(), e.getBusinessName(),
                e.getBusinessRegistrationNumber(), e.getContactNationalId(), e.getRegisteredAt(), e.getStatus().name());
    }
}
