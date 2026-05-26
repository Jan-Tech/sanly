package com.sanly.dmv.repository;

import com.sanly.dmv.entity.DmvOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<DmvOfficer, Long> {
    Optional<DmvOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
}
