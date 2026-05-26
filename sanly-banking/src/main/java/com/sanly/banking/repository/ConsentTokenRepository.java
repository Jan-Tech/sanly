package com.sanly.banking.repository;

import com.sanly.banking.entity.ConsentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentTokenRepository extends JpaRepository<ConsentToken, UUID> {

    Optional<ConsentToken> findByTokenHash(String tokenHash);

    Optional<ConsentToken> findByConsentCode(String consentCode);

    List<ConsentToken> findByStatusAndExpiresAtBefore(String status, LocalDateTime cutoff);
}
