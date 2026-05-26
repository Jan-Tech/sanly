package com.sanly.court.repository;

import com.sanly.court.entity.CourtOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourtOfficerRepository extends JpaRepository<CourtOfficer, UUID> {
    Optional<CourtOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
}
