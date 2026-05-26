package com.sanly.bridge.repository;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.InstitutionPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionPermissionRepository extends JpaRepository<InstitutionPermission, Long> {

    /**
     * Core permission check: returns true when an active permission exists
     * for the (requestingCode, targetCode, dataType) triple.
     */
    boolean existsByRequestingCodeAndTargetCodeAndDataTypeAndActiveTrue(
            String requestingCode, String targetCode, DataType dataType);

    List<InstitutionPermission> findAllByActiveTrue();

    List<InstitutionPermission> findAllByRequestingCodeAndActiveTrue(String requestingCode);

    List<InstitutionPermission> findAllByTargetCodeAndActiveTrue(String targetCode);

    Optional<InstitutionPermission> findByRequestingCodeAndTargetCodeAndDataType(
            String requestingCode, String targetCode, DataType dataType);
}
