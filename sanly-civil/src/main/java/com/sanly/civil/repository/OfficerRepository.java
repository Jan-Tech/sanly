package com.sanly.civil.repository;

import com.sanly.civil.entity.CivilOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficerRepository extends JpaRepository<CivilOfficer, Long> {
    Optional<CivilOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
}
