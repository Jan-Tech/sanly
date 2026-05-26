package com.sanly.banking.repository;

import com.sanly.banking.entity.BankingCodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankingCodeSequenceRepository extends JpaRepository<BankingCodeSequence, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BankingCodeSequence s WHERE s.sequenceType = :type")
    Optional<BankingCodeSequence> findBySequenceTypeWithLock(@Param("type") String type);
}
