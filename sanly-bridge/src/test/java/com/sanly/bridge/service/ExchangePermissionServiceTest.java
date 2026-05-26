package com.sanly.bridge.service;

import com.sanly.bridge.dto.request.PermissionCreateRequest;
import com.sanly.bridge.dto.response.PermissionResponse;
import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.InstitutionPermission;
import com.sanly.bridge.exception.InstitutionNotFoundException;
import com.sanly.bridge.exception.PermissionNotFoundException;
import com.sanly.bridge.repository.InstitutionPermissionRepository;
import com.sanly.bridge.repository.InstitutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService")
class ExchangePermissionServiceTest {

    private static final String REQUESTING = "INST_TAX";
    private static final String TARGET     = "INST_POLICE";
    private static final DataType DT       = DataType.CRIMINAL_RECORD;

    @Mock InstitutionPermissionRepository permissionRepository;
    @Mock InstitutionRepository           institutionRepository;

    @InjectMocks PermissionServiceImpl service;

    // ── grant() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("grant()")
    class GrantTests {

        @Test
        @DisplayName("creates a new active permission when none exists")
        void grant_newPermission_savedAndReturned() {
            when(institutionRepository.existsByInstitutionCode(REQUESTING)).thenReturn(true);
            when(institutionRepository.existsByInstitutionCode(TARGET)).thenReturn(true);
            when(permissionRepository.findByRequestingCodeAndTargetCodeAndDataType(
                    REQUESTING, TARGET, DT)).thenReturn(Optional.empty());
            when(permissionRepository.existsByRequestingCodeAndTargetCodeAndDataTypeAndActiveTrue(
                    REQUESTING, TARGET, DT)).thenReturn(false);

            InstitutionPermission saved = buildPerm(1L, true);
            when(permissionRepository.save(any())).thenReturn(saved);

            PermissionResponse resp = service.grant(buildRequest(), "admin");

            assertThat(resp.getId()).isEqualTo(1L);
            assertThat(resp.getRequestingCode()).isEqualTo(REQUESTING);
            assertThat(resp.getTargetCode()).isEqualTo(TARGET);
            assertThat(resp.getDataType()).isEqualTo(DT);
            assertThat(resp.isActive()).isTrue();
        }

        @Test
        @DisplayName("re-activates a previously revoked permission")
        void grant_revokedPermission_reactivated() {
            InstitutionPermission revoked = buildPerm(2L, false);
            when(institutionRepository.existsByInstitutionCode(REQUESTING)).thenReturn(true);
            when(institutionRepository.existsByInstitutionCode(TARGET)).thenReturn(true);
            when(permissionRepository.findByRequestingCodeAndTargetCodeAndDataType(
                    REQUESTING, TARGET, DT)).thenReturn(Optional.of(revoked));
            when(permissionRepository.existsByRequestingCodeAndTargetCodeAndDataTypeAndActiveTrue(
                    REQUESTING, TARGET, DT)).thenReturn(true);
            when(permissionRepository.findByRequestingCodeAndTargetCodeAndDataType(
                    REQUESTING, TARGET, DT)).thenReturn(Optional.of(revoked));

            service.grant(buildRequest(), "admin");

            verify(permissionRepository).save(argThat(InstitutionPermission::isActive));
        }

        @Test
        @DisplayName("returns existing permission idempotently if already active")
        void grant_alreadyActive_returnsExisting() {
            InstitutionPermission existing = buildPerm(3L, true);
            when(institutionRepository.existsByInstitutionCode(REQUESTING)).thenReturn(true);
            when(institutionRepository.existsByInstitutionCode(TARGET)).thenReturn(true);
            when(permissionRepository.findByRequestingCodeAndTargetCodeAndDataType(
                    REQUESTING, TARGET, DT)).thenReturn(Optional.of(existing));
            when(permissionRepository.existsByRequestingCodeAndTargetCodeAndDataTypeAndActiveTrue(
                    REQUESTING, TARGET, DT)).thenReturn(true);

            PermissionResponse resp = service.grant(buildRequest(), "admin");

            assertThat(resp.getId()).isEqualTo(3L);
            verify(permissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InstitutionNotFoundException when requesting institution missing")
        void grant_unknownRequestingInstitution_throws() {
            when(institutionRepository.existsByInstitutionCode(REQUESTING)).thenReturn(false);

            assertThatThrownBy(() -> service.grant(buildRequest(), "admin"))
                    .isInstanceOf(InstitutionNotFoundException.class);
        }

        @Test
        @DisplayName("throws InstitutionNotFoundException when target institution missing")
        void grant_unknownTargetInstitution_throws() {
            when(institutionRepository.existsByInstitutionCode(REQUESTING)).thenReturn(true);
            when(institutionRepository.existsByInstitutionCode(TARGET)).thenReturn(false);

            assertThatThrownBy(() -> service.grant(buildRequest(), "admin"))
                    .isInstanceOf(InstitutionNotFoundException.class);
        }
    }

    // ── revoke() ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("revoke()")
    class RevokeTests {

        @Test
        @DisplayName("marks permission as inactive")
        void revoke_setsActiveToFalse() {
            InstitutionPermission perm = buildPerm(10L, true);
            when(permissionRepository.findById(10L)).thenReturn(Optional.of(perm));
            when(permissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.revoke(10L);

            verify(permissionRepository).save(argThat(p -> !p.isActive()));
        }

        @Test
        @DisplayName("throws PermissionNotFoundException for unknown id")
        void revoke_notFound_throws() {
            when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.revoke(99L))
                    .isInstanceOf(PermissionNotFoundException.class);
        }
    }

    // ── listAll() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll() returns only active permissions")
    void listAll_returnsActiveOnly() {
        when(permissionRepository.findAllByActiveTrue())
                .thenReturn(List.of(buildPerm(1L, true), buildPerm(2L, true)));

        List<PermissionResponse> result = service.listAll();

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(r -> assertThat(r.isActive()).isTrue());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PermissionCreateRequest buildRequest() {
        PermissionCreateRequest req = new PermissionCreateRequest();
        req.setRequestingCode(REQUESTING);
        req.setTargetCode(TARGET);
        req.setDataType(DT);
        return req;
    }

    private InstitutionPermission buildPerm(Long id, boolean active) {
        return InstitutionPermission.builder()
                .id(id)
                .requestingCode(REQUESTING)
                .targetCode(TARGET)
                .dataType(DT)
                .grantedBy("admin")
                .grantedAt(LocalDateTime.now())
                .active(active)
                .build();
    }
}
