package com.sanly.banking.repository;

import com.sanly.banking.entity.BankDataAccess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface BankDataAccessRepository extends JpaRepository<BankDataAccess, UUID> {

    Page<BankDataAccess> findByCitizenNationalIdOrderByAccessedAtDesc(String citizenNationalId, Pageable pageable);

    Page<BankDataAccess> findByBankCodeOrderByAccessedAtDesc(String bankCode, Pageable pageable);

    Page<BankDataAccess> findAllByOrderByAccessedAtDesc(Pageable pageable);

    long countByAccessedAtAfter(LocalDateTime since);
}
