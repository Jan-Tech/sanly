package com.sanly.education.repository;

import com.sanly.education.entity.EducationOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EducationOfficerRepository extends JpaRepository<EducationOfficer, UUID> {
    Optional<EducationOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
}
