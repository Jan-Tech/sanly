package com.sanly.pension.repository;

import com.sanly.pension.entity.AccountStatus;
import com.sanly.pension.entity.PensionAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PensionAccountRepository extends JpaRepository<PensionAccount, Long> {
    Optional<PensionAccount> findByAccountCode(String accountCode);
    Optional<PensionAccount> findByCitizenNationalId(String citizenNationalId);
    List<PensionAccount> findByStatus(AccountStatus status);

    @Query("SELECT a FROM PensionAccount a WHERE a.status = 'ACCUMULATING' AND a.eligibleAt <= :today")
    List<PensionAccount> findNowEligible(@Param("today") LocalDate today);
}
