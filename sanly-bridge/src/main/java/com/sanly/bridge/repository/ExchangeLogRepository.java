package com.sanly.bridge.repository;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.ExchangeLog;
import com.sanly.bridge.entity.ExchangeResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ExchangeLogRepository extends JpaRepository<ExchangeLog, Long> {

    /** Full audit search with every filter optional. */
    @Query("""
        SELECT e FROM ExchangeLog e
        WHERE (:institutionCode IS NULL
                 OR e.requestingCode = :institutionCode
                 OR e.targetCode     = :institutionCode)
          AND (:nationalId IS NULL OR e.nationalId = :nationalId)
          AND (:dataType   IS NULL OR e.dataType   = :dataType)
          AND (:result     IS NULL OR e.result     = :result)
          AND (:from       IS NULL OR e.exchangedAt >= :from)
          AND (:to         IS NULL OR e.exchangedAt <= :to)
        ORDER BY e.exchangedAt DESC
        """)
    Page<ExchangeLog> findWithFilters(
            @Param("institutionCode") String institutionCode,
            @Param("nationalId")     String nationalId,
            @Param("dataType")       DataType dataType,
            @Param("result")         ExchangeResult result,
            @Param("from")           LocalDateTime from,
            @Param("to")             LocalDateTime to,
            Pageable pageable
    );

    Page<ExchangeLog> findByNationalIdOrderByExchangedAtDesc(String nationalId, Pageable pageable);

    // ── Anomaly Detection queries ─────────────────────────────────────────────

    /** Rule 1 — count QUERY operations by institution in a time window. */
    @Query("""
        SELECT COUNT(e) FROM ExchangeLog e
        WHERE e.requestingCode = :code
          AND e.operationType  = com.sanly.bridge.entity.OperationType.QUERY
          AND e.exchangedAt   >= :since
        """)
    long countQueryByInstitutionSince(
            @Param("code")  String code,
            @Param("since") LocalDateTime since
    );

    /** Rule 2 — count queries by institution + nationalId in a time window. */
    @Query("""
        SELECT COUNT(e) FROM ExchangeLog e
        WHERE e.requestingCode = :code
          AND e.nationalId     = :nationalId
          AND e.operationType  = com.sanly.bridge.entity.OperationType.QUERY
          AND e.exchangedAt   >= :since
        """)
    long countQueryByInstitutionAndNationalIdSince(
            @Param("code")       String code,
            @Param("nationalId") String nationalId,
            @Param("since")      LocalDateTime since
    );

    /** Rule 5 — count distinct citizen NINs queried by an institution in a time window. */
    @Query("""
        SELECT COUNT(DISTINCT e.nationalId) FROM ExchangeLog e
        WHERE e.requestingCode = :code
          AND e.operationType  = com.sanly.bridge.entity.OperationType.QUERY
          AND e.nationalId    IS NOT NULL
          AND e.exchangedAt   >= :since
        """)
    long countDistinctNationalIdsByInstitutionSince(
            @Param("code")  String code,
            @Param("since") LocalDateTime since
    );

    /** Rule 6 — count DENIED results by institution in a time window. */
    @Query("""
        SELECT COUNT(e) FROM ExchangeLog e
        WHERE e.requestingCode = :code
          AND e.result         = com.sanly.bridge.entity.ExchangeResult.DENIED
          AND e.exchangedAt   >= :since
        """)
    long countDeniedByInstitutionSince(
            @Param("code")  String code,
            @Param("since") LocalDateTime since
    );
}
