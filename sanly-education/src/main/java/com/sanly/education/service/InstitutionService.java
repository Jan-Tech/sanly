package com.sanly.education.service;

import com.sanly.education.dto.request.RegisterInstitutionRequest;
import com.sanly.education.dto.response.InstitutionResponse;
import com.sanly.education.entity.EducationInstitution;
import com.sanly.education.entity.InstitutionStatus;
import com.sanly.education.exception.DuplicateResourceException;
import com.sanly.education.exception.RecordNotFoundException;
import com.sanly.education.repository.EducationInstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {
    private final EducationInstitutionRepository institutionRepository;

    @Transactional
    public InstitutionResponse register(RegisterInstitutionRequest req) {
        int nextSeq = institutionRepository.findMaxInstitutionSequence() + 1;
        String code = String.format("TM-EDU-%04d", nextSeq);

        if (institutionRepository.existsByInstitutionCode(code)) {
            throw new DuplicateResourceException("Institution code collision: " + code);
        }

        EducationInstitution inst = EducationInstitution.builder()
                .institutionCode(code)
                .name(req.name())
                .type(req.type())
                .region(req.region())
                .address(req.address())
                .licenseNumber(req.licenseNumber())
                .accreditedUntil(req.accreditedUntil())
                .status(InstitutionStatus.ACTIVE)
                .build();
        return InstitutionResponse.from(institutionRepository.save(inst));
    }

    public List<InstitutionResponse> findAll() {
        return institutionRepository.findAll().stream().map(InstitutionResponse::from).toList();
    }

    public InstitutionResponse findByCode(String code) {
        return InstitutionResponse.from(getOrThrow(code));
    }

    @Transactional
    public InstitutionResponse updateStatus(String code, InstitutionStatus status) {
        EducationInstitution inst = getOrThrow(code);
        inst.setStatus(status);
        return InstitutionResponse.from(institutionRepository.save(inst));
    }

    public EducationInstitution getOrThrow(String code) {
        return institutionRepository.findByInstitutionCode(code)
                .orElseThrow(() -> new RecordNotFoundException("Institution not found: " + code));
    }
}
