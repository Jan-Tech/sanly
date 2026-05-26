package com.sanly.police.repository;

import com.sanly.police.entity.CriminalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CriminalRecordRepository extends JpaRepository<CriminalRecord, UUID> {
    List<CriminalRecord> findByCitizenNationalIdOrderByCreatedAtDesc(String nationalId);
}
