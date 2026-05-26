package com.sanly.bridge.service;

import com.sanly.bridge.dto.request.PermissionCreateRequest;
import com.sanly.bridge.dto.response.PermissionResponse;
import com.sanly.bridge.entity.InstitutionPermission;
import com.sanly.bridge.exception.InstitutionNotFoundException;
import com.sanly.bridge.exception.PermissionNotFoundException;
import com.sanly.bridge.repository.InstitutionPermissionRepository;
import com.sanly.bridge.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final InstitutionPermissionRepository permissionRepository;
    private final InstitutionRepository           institutionRepository;

    @Override
    @Transactional
    public PermissionResponse grant(PermissionCreateRequest req, String grantedBy) {
        validateInstitutionExists(req.getRequestingCode());
        validateInstitutionExists(req.getTargetCode());

        // Re-activate if a revoked permission for the same triple exists
        permissionRepository
                .findByRequestingCodeAndTargetCodeAndDataType(
                        req.getRequestingCode(), req.getTargetCode(), req.getDataType())
                .ifPresent(existing -> {
                    if (!existing.isActive()) {
                        existing.setActive(true);
                        permissionRepository.save(existing);
                        log.info("Re-activated permission id={}", existing.getId());
                    }
                });

        // If the record already exists (and is active), the unique constraint
        // means the above block handled it. Now do a fresh insert only if absent.
        boolean alreadyActive = permissionRepository
                .existsByRequestingCodeAndTargetCodeAndDataTypeAndActiveTrue(
                        req.getRequestingCode(), req.getTargetCode(), req.getDataType());

        if (alreadyActive) {
            // Return existing permission as-is
            return permissionRepository
                    .findByRequestingCodeAndTargetCodeAndDataType(
                            req.getRequestingCode(), req.getTargetCode(), req.getDataType())
                    .map(this::toResponse)
                    .orElseThrow();
        }

        InstitutionPermission perm = InstitutionPermission.builder()
                .requestingCode(req.getRequestingCode())
                .targetCode(req.getTargetCode())
                .dataType(req.getDataType())
                .grantedBy(grantedBy)
                .build();

        InstitutionPermission saved;
        try {
            saved = permissionRepository.save(perm);
        } catch (DataIntegrityViolationException e) {
            // Race condition — permission was created concurrently
            saved = permissionRepository
                    .findByRequestingCodeAndTargetCodeAndDataType(
                            req.getRequestingCode(), req.getTargetCode(), req.getDataType())
                    .orElseThrow();
        }

        log.info("Granted permission: {} → {} [{}]",
                req.getRequestingCode(), req.getTargetCode(), req.getDataType());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> listAll() {
        return permissionRepository.findAllByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void revoke(Long id) {
        InstitutionPermission perm = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
        perm.setActive(false);
        permissionRepository.save(perm);
        log.info("Revoked permission id={}", id);
    }

    // ---- helpers ----

    private void validateInstitutionExists(String code) {
        if (!institutionRepository.existsByInstitutionCode(code)) {
            throw new InstitutionNotFoundException(code);
        }
    }

    private PermissionResponse toResponse(InstitutionPermission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .requestingCode(p.getRequestingCode())
                .targetCode(p.getTargetCode())
                .dataType(p.getDataType())
                .grantedBy(p.getGrantedBy())
                .grantedAt(p.getGrantedAt())
                .active(p.isActive())
                .build();
    }
}
