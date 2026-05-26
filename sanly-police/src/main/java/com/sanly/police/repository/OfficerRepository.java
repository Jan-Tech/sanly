package com.sanly.police.repository;

import com.sanly.police.entity.PoliceOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<PoliceOfficer, Long> {
    Optional<PoliceOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
}
