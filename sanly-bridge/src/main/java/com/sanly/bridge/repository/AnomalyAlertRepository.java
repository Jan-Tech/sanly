package com.sanly.bridge.repository;

import com.sanly.bridge.entity.AlertSeverity;
import com.sanly.bridge.entity.AlertStatus;
import com.sanly.bridge.entity.AnomalyAlert;
import com.sanly.bridge.entity.AnomalyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AnomalyAlertRepository extends JpaRepository<AnomalyAlert, UUID> {

    /** Deduplication guard — do not create duplicate alerts for the same pattern. */
    boolean existsByInstitutionCodeAndAlertTypeAndStatusAndDetectedAtAfter(
            String institutionCode,
            AnomalyType alertType,
            AlertStatus status,
            LocalDateTime after
    );

    long countByStatus(AlertStatus status);

    long countByStatusAndSeverity(AlertStatus status, AlertSeverity severity);

    @Query("""
        SELECT a FROM AnomalyAlert a
        WHERE (:institutionCode IS NULL OR a.institutionCode = :institutionCode)
          AND (:severity        IS NULL OR a.severity        = :severity)
          AND (:status          IS NULL OR a.status          = :status)
          AND (:from            IS NULL OR a.detectedAt     >= :from)
          AND (:to              IS NULL OR a.detectedAt     <= :to)
        ORDER BY a.detectedAt DESC
        """)
    Page<AnomalyAlert> findWithFilters(
            @Param("institutionCode") String institutionCode,
            @Param("severity")        AlertSeverity severity,
            @Param("status")          AlertStatus status,
            @Param("from")            LocalDateTime from,
            @Param("to")              LocalDateTime to,
            Pageable pageable
    );
}
