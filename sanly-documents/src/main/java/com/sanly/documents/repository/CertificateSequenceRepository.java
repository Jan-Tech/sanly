package com.sanly.documents.repository;

import com.sanly.documents.entity.CertificateSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CertificateSequenceRepository extends JpaRepository<CertificateSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CertificateSequence s WHERE s.year = :year")
    Optional<CertificateSequence> findByYearWithLock(@Param("year") int year);

    @Modifying
    @Query(value = "INSERT INTO certificate_sequences (year, last_sequence) VALUES (:year, 0) ON CONFLICT (year) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(@Param("year") int year);
}
