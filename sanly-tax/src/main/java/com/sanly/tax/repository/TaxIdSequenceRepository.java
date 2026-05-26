package com.sanly.tax.repository;

import com.sanly.tax.entity.TaxIdSequence;
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
public interface TaxIdSequenceRepository extends JpaRepository<TaxIdSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TaxIdSequence s WHERE s.year = :year")
    Optional<TaxIdSequence> findByYearWithLock(@Param("year") int year);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO tax_id_sequences (year, last_counter) VALUES (:year, 0) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(@Param("year") int year);
}
