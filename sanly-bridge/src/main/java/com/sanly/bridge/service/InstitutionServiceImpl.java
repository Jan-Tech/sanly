package com.sanly.bridge.service;

import com.sanly.bridge.dto.request.InstitutionCreateRequest;
import com.sanly.bridge.dto.request.InstitutionStatusRequest;
import com.sanly.bridge.dto.response.InstitutionKeyResponse;
import com.sanly.bridge.dto.response.InstitutionResponse;
import com.sanly.bridge.entity.Institution;
import com.sanly.bridge.exception.DuplicateInstitutionException;
import com.sanly.bridge.exception.InstitutionNotFoundException;
import com.sanly.bridge.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final ApiKeyService         apiKeyService;

    @Override
    @Transactional
    public InstitutionKeyResponse register(InstitutionCreateRequest req) {
        if (institutionRepository.existsByInstitutionCode(req.getInstitutionCode())) {
            throw new DuplicateInstitutionException(req.getInstitutionCode());
        }

        String rawKey = apiKeyService.generateRawKey();

        Institution institution = Institution.builder()
                .institutionCode(req.getInstitutionCode())
                .name(req.getName())
                .description(req.getDescription())
                .hashedApiKey(apiKeyService.hash(rawKey))
                .publishableTypes(req.getPublishableTypes() != null
                        ? new HashSet<>(req.getPublishableTypes())
                        : new HashSet<>())
                .build();

        Institution saved = institutionRepository.save(institution);
        log.info("Registered institution: {}", saved.getInstitutionCode());

        return InstitutionKeyResponse.builder()
                .institutionCode(saved.getInstitutionCode())
                .name(saved.getName())
                .publishableTypes(saved.getPublishableTypes())
                .status(saved.getStatus())
                .apiKey(rawKey)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstitutionResponse> listAll() {
        return institutionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InstitutionResponse getByCode(String code) {
        return toResponse(findOrThrow(code));
    }

    @Override
    @Transactional
    public InstitutionResponse updateStatus(String code, InstitutionStatusRequest req) {
        Institution institution = findOrThrow(code);
        institution.setStatus(req.getStatus());
        log.info("Institution {} status changed to {}", code, req.getStatus());
        return toResponse(institutionRepository.save(institution));
    }

    @Override
    @Transactional
    public InstitutionKeyResponse rotateKey(String code) {
        Institution institution = findOrThrow(code);
        String rawKey = apiKeyService.generateRawKey();
        institution.setHashedApiKey(apiKeyService.hash(rawKey));
        institutionRepository.save(institution);
        log.info("API key rotated for institution: {}", code);

        return InstitutionKeyResponse.builder()
                .institutionCode(institution.getInstitutionCode())
                .name(institution.getName())
                .publishableTypes(institution.getPublishableTypes())
                .status(institution.getStatus())
                .apiKey(rawKey)
                .createdAt(institution.getCreatedAt())
                .build();
    }

    // ---- helpers ----

    private Institution findOrThrow(String code) {
        return institutionRepository.findByInstitutionCode(code)
                .orElseThrow(() -> new InstitutionNotFoundException(code));
    }

    private InstitutionResponse toResponse(Institution i) {
        return InstitutionResponse.builder()
                .institutionCode(i.getInstitutionCode())
                .name(i.getName())
                .description(i.getDescription())
                .publishableTypes(i.getPublishableTypes())
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
