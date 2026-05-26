package com.sanly.education.repository;

import com.sanly.education.entity.DiplomaSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DiplomaSequenceRepository extends JpaRepository<DiplomaSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DiplomaSequence s WHERE s.year = :year")
    Optional<DiplomaSequence> findByYearWithLock(int year);

    @Modifying
    @Query(value = "INSERT INTO diploma_sequences (year, next_value) VALUES (:year, 1) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(int year);
}
