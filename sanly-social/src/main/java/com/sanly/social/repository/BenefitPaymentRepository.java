package com.sanly.social.repository;

import com.sanly.social.entity.BenefitPayment;
import com.sanly.social.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenefitPaymentRepository extends JpaRepository<BenefitPayment, UUID> {
    List<BenefitPayment> findByClaimCode(String claimCode);
    List<BenefitPayment> findByCitizenNationalId(String citizenNationalId);
    List<BenefitPayment> findByStatus(PaymentStatus status);
    Optional<BenefitPayment> findByClaimCodeAndPaymentPeriod(String claimCode, String paymentPeriod);
    boolean existsByClaimCodeAndPaymentPeriod(String claimCode, String paymentPeriod);

    @Query("SELECT p FROM BenefitPayment p WHERE p.status = 'SCHEDULED' AND p.paymentPeriod = :period")
    List<BenefitPayment> findScheduledForPeriod(String period);
}
