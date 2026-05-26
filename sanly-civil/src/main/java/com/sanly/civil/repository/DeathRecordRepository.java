package com.sanly.civil.repository;

import com.sanly.civil.entity.DeathRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeathRecordRepository extends JpaRepository<DeathRecord, UUID> {
    Optional<DeathRecord> findByDeceasedNationalId(String deceasedNationalId);
    Optional<DeathRecord> findByCertificateNumber(String certificateNumber);
    boolean existsByDeceasedNationalId(String deceasedNationalId);
}
