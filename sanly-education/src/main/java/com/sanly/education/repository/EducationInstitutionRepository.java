package com.sanly.education.repository;

import com.sanly.education.entity.EducationInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EducationInstitutionRepository extends JpaRepository<EducationInstitution, UUID> {
    Optional<EducationInstitution> findByInstitutionCode(String institutionCode);
    boolean existsByInstitutionCode(String institutionCode);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.institutionCode, 8) AS int)), 0) FROM EducationInstitution i WHERE i.institutionCode LIKE 'TM-EDU-%'")
    int findMaxInstitutionSequence();
}
