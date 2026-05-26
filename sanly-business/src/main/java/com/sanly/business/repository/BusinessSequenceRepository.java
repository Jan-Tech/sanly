package com.sanly.business.repository;

import com.sanly.business.entity.BusinessSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BusinessSequenceRepository extends JpaRepository<BusinessSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BusinessSequence s WHERE s.year = :year")
    Optional<BusinessSequence> findByYearWithLock(@Param("year") int year);

    @Modifying
    @Query(value = "INSERT INTO business_sequences (year, last_value) VALUES (:year, 0) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    void insertIfNotExists(@Param("year") int year);
}
