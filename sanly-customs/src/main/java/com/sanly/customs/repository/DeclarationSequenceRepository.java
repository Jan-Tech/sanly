package com.sanly.customs.repository;

import com.sanly.customs.entity.DeclarationSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DeclarationSequenceRepository extends JpaRepository<DeclarationSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DeclarationSequence s WHERE s.year = :year")
    Optional<DeclarationSequence> findByYearWithLock(int year);

    @Modifying
    @Query(value = "INSERT INTO declaration_sequences (year, next_value) VALUES (:year, 1) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(int year);
}
