package com.sanly.education.repository;

import com.sanly.education.entity.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, UUID> {
    List<AcademicRecord> findByCitizenNationalId(String citizenNationalId);
    List<AcademicRecord> findByCitizenNationalIdAndInstitutionCode(
            String citizenNationalId, String institutionCode);
}
