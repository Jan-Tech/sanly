package com.sanly.civil.repository;

import com.sanly.civil.entity.BirthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BirthRecordRepository extends JpaRepository<BirthRecord, UUID> {
    Optional<BirthRecord> findByChildNationalId(String childNationalId);
    Optional<BirthRecord> findByCertificateNumber(String certificateNumber);
    boolean existsByChildNationalId(String childNationalId);
}
