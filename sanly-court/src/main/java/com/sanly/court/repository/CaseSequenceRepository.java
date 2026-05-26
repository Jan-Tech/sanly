package com.sanly.court.repository;

import com.sanly.court.entity.CaseSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CaseSequenceRepository extends JpaRepository<CaseSequence, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CaseSequence s WHERE s.year = :year")
    Optional<CaseSequence> findByYearWithLock(int year);

    @Modifying
    @Query(value = "INSERT INTO case_sequences (year, next_value) VALUES (:year, 1) ON CONFLICT (year) DO NOTHING", nativeQuery = true)
    void insertIfNotExists(int year);
}
