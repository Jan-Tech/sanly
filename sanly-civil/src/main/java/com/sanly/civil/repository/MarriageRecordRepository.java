package com.sanly.civil.repository;

import com.sanly.civil.entity.MarriageRecord;
import com.sanly.civil.entity.MarriageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarriageRecordRepository extends JpaRepository<MarriageRecord, UUID> {
    Optional<MarriageRecord> findByCertificateNumber(String certificateNumber);

    @Query("SELECT m FROM MarriageRecord m WHERE (m.spouse1NationalId = :nationalId OR m.spouse2NationalId = :nationalId) AND m.status = 'ACTIVE'")
    List<MarriageRecord> findActiveBySpouseNationalId(@Param("nationalId") String nationalId);

    Page<MarriageRecord> findByStatus(MarriageStatus status, Pageable pageable);
}
