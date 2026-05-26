package com.sanly.civil.repository;

import com.sanly.civil.entity.CertificateSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CertificateSequenceRepository extends JpaRepository<CertificateSequence, CertificateSequence.CertificateSequenceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CertificateSequence s WHERE s.id.seqType = :seqType AND s.id.year = :year")
    Optional<CertificateSequence> findBySeqTypeAndYearWithLock(@Param("seqType") String seqType,
                                                                @Param("year") int year);

    @Modifying
    @Query(value = "INSERT INTO certificate_sequences (seq_type, year, last_value) VALUES (:seqType, :year, 0) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    void insertIfNotExists(@Param("seqType") String seqType, @Param("year") int year);
}
