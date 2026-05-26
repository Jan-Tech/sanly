package com.sanly.signature.repository;

import com.sanly.signature.entity.SignatureSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SignatureSequenceRepository extends JpaRepository<SignatureSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SignatureSequence s WHERE s.year = :year")
    Optional<SignatureSequence> findByYearWithLock(@Param("year") int year);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO signature_sequences (year, last_sequence) VALUES (:year, 0) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(@Param("year") int year);
}
