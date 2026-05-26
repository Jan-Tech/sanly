package com.sanly.customs.repository;

import com.sanly.customs.entity.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, UUID> {
    List<InspectionRecord> findByDeclarationCodeOrderByCreatedAtDesc(String declarationCode);
}
