package com.sanly.social.repository;

import com.sanly.social.entity.SocialOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SocialOfficerRepository extends JpaRepository<SocialOfficer, UUID> {
    Optional<SocialOfficer> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
}
