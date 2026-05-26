package com.sanly.social.repository;

import com.sanly.social.entity.ClaimSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClaimSequenceRepository extends JpaRepository<ClaimSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ClaimSequence s WHERE s.year = :year")
    Optional<ClaimSequence> findByYearWithLock(int year);

    @Modifying
    @Query(value = "INSERT INTO claim_sequences (year, next_value) VALUES (:year, 1) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(int year);
}
