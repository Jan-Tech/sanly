package com.sanly.analytics.repository;

import com.sanly.analytics.entity.AnalyticsOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalyticsOfficerRepository extends JpaRepository<AnalyticsOfficer, UUID> {
    Optional<AnalyticsOfficer> findByUsernameAndActiveTrue(String username);
}
