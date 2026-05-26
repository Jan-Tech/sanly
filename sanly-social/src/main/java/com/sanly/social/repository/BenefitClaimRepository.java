package com.sanly.social.repository;

import com.sanly.social.entity.BenefitClaim;
import com.sanly.social.entity.ClaimStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenefitClaimRepository extends JpaRepository<BenefitClaim, UUID> {
    Optional<BenefitClaim> findByClaimCode(String claimCode);
    List<BenefitClaim> findByCitizenNationalId(String citizenNationalId);
    List<BenefitClaim> findByStatus(ClaimStatus status);
    List<BenefitClaim> findByCitizenNationalIdAndStatus(String citizenNationalId, ClaimStatus status);

    @Query("SELECT c FROM BenefitClaim c WHERE c.citizenNationalId = :nationalId AND c.status IN ('PENDING','APPROVED','ACTIVE')")
    List<BenefitClaim> findActiveOrPendingByCitizen(String nationalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM BenefitClaim c WHERE c.claimCode = :claimCode")
    Optional<BenefitClaim> findByClaimCodeWithLock(String claimCode);

    @Modifying
    @Query("UPDATE BenefitClaim c SET c.status = 'CANCELLED' WHERE c.citizenNationalId = :nationalId AND c.status IN ('PENDING','APPROVED','ACTIVE','SUSPENDED')")
    int cancelAllForCitizen(String nationalId);
}
