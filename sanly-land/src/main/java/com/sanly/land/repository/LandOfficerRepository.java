package com.sanly.land.repository;

import com.sanly.land.entity.LandOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LandOfficerRepository extends JpaRepository<LandOfficer, UUID> {
    Optional<LandOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
}
