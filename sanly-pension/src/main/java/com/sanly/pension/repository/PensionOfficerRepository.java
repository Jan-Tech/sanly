package com.sanly.pension.repository;

import com.sanly.pension.entity.PensionOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PensionOfficerRepository extends JpaRepository<PensionOfficer, Long> {
    Optional<PensionOfficer> findByUsername(String username);
}
