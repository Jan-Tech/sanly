package com.sanly.business.repository;

import com.sanly.business.entity.RegistrationOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficerRepository extends JpaRepository<RegistrationOfficer, Long> {
    Optional<RegistrationOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
}
