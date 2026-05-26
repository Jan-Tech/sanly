package com.sanly.education.repository;

import com.sanly.education.entity.Diploma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiplomaRepository extends JpaRepository<Diploma, UUID> {
    Optional<Diploma> findByDiplomaCode(String diplomaCode);
    List<Diploma> findByCitizenNationalId(String citizenNationalId);
    List<Diploma> findByInstitutionCode(String institutionCode);
    boolean existsByDiplomaCode(String diplomaCode);
}
