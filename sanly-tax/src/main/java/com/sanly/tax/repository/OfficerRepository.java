package com.sanly.tax.repository;

import com.sanly.tax.entity.TaxOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<TaxOfficer, Long> {
    Optional<TaxOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
}
