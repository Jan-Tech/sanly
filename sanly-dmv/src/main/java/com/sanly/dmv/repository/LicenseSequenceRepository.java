package com.sanly.dmv.repository;

import com.sanly.dmv.entity.LicenseSequence;
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
public interface LicenseSequenceRepository extends JpaRepository<LicenseSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM LicenseSequence s WHERE s.year = :year")
    Optional<LicenseSequence> findByYearWithLock(@Param("year") Integer year);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO license_sequences (year, last_counter)
        VALUES (:year, 0)
        ON CONFLICT (year) DO NOTHING
        """, nativeQuery = true)
    void insertIfNotExists(@Param("year") Integer year);
}
