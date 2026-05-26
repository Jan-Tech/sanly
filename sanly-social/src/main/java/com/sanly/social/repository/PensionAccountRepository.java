package com.sanly.social.repository;

import com.sanly.social.entity.PensionAccount;
import com.sanly.social.entity.PensionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PensionAccountRepository extends JpaRepository<PensionAccount, UUID> {
    Optional<PensionAccount> findByCitizenNationalId(String citizenNationalId);
    boolean existsByCitizenNationalId(String citizenNationalId);

    // For daily eligibility check
    @Query("SELECT p FROM PensionAccount p WHERE p.status = 'ACCUMULATING' AND p.eligibleAt <= :today")
    List<PensionAccount> findNewlyEligible(LocalDate today);

    List<PensionAccount> findByStatus(PensionStatus status);
}
