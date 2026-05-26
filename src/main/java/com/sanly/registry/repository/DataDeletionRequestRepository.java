package com.sanly.registry.repository;

import com.sanly.registry.entity.AffectedService;
import com.sanly.registry.entity.DataDeletionRequest;
import com.sanly.registry.entity.RequestStatus;
import com.sanly.registry.entity.RequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataDeletionRequestRepository extends JpaRepository<DataDeletionRequest, UUID> {

    Optional<DataDeletionRequest> findByRequestCode(String requestCode);

    Page<DataDeletionRequest> findByCitizenNationalIdOrderBySubmittedAtDesc(String nationalId, Pageable pageable);

    Page<DataDeletionRequest> findByStatusOrderBySubmittedAtDesc(RequestStatus status, Pageable pageable);

    Page<DataDeletionRequest> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    long countByCitizenNationalIdAndSubmittedAtAfter(String nationalId, LocalDateTime since);

    @Query("""
        SELECT r FROM DataDeletionRequest r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:requestType IS NULL OR r.requestType = :requestType)
          AND (:affectedService IS NULL OR r.affectedService = :affectedService)
        ORDER BY r.submittedAt DESC
        """)
    Page<DataDeletionRequest> findWithFilters(
        @Param("status") RequestStatus status,
        @Param("requestType") RequestType requestType,
        @Param("affectedService") AffectedService affectedService,
        Pageable pageable);
}
