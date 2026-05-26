package com.sanly.customs.repository;

import com.sanly.customs.entity.CustomsOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomsOfficerRepository extends JpaRepository<CustomsOfficer, UUID> {
    Optional<CustomsOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
    List<CustomsOfficer> findByPort(String port);
}
