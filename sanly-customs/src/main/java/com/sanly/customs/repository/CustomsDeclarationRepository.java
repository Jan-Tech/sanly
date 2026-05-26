package com.sanly.customs.repository;

import com.sanly.customs.entity.CustomsDeclaration;
import com.sanly.customs.entity.DeclarationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomsDeclarationRepository extends JpaRepository<CustomsDeclaration, UUID> {
    Optional<CustomsDeclaration> findByDeclarationCode(String declarationCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM CustomsDeclaration d WHERE d.declarationCode = :code")
    Optional<CustomsDeclaration> findByDeclarationCodeWithLock(String code);

    List<CustomsDeclaration> findByDeclarantNationalId(String nationalId);
    List<CustomsDeclaration> findByDeclarantBusinessNumber(String businessNumber);
    List<CustomsDeclaration> findByPortCodeAndStatus(String portCode, DeclarationStatus status);
    List<CustomsDeclaration> findByPortCode(String portCode);
    List<CustomsDeclaration> findByStatus(DeclarationStatus status);

    // For duties reminder: SUBMITTED declarations older than N days with no duties paid
    @Query("SELECT d FROM CustomsDeclaration d WHERE d.status = 'SUBMITTED' AND d.createdAt <= :cutoff AND (d.dutiesPaid IS NULL OR d.dutiesPaid = '0')")
    List<CustomsDeclaration> findSubmittedWithUnpaidDutiesOlderThan(LocalDateTime cutoff);
}
