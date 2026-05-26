package com.sanly.land.repository;

import com.sanly.land.entity.PropertySequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PropertySequenceRepository extends JpaRepository<PropertySequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM PropertySequence s WHERE s.year = :year")
    Optional<PropertySequence> findByYearWithLock(int year);

    @Modifying
    @Query(value = "INSERT INTO property_sequences (year, next_value) VALUES (:year, 1) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(int year);
}
