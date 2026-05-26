package com.sanly.signature.repository;

import com.sanly.signature.entity.SignatureRecord;
import com.sanly.signature.entity.SignatureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, UUID> {

    Optional<SignatureRecord> findBySignatureCode(String signatureCode);

    Page<SignatureRecord> findBySignerNationalIdOrderBySignedAtDesc(String signerNationalId, Pageable pageable);

    Page<SignatureRecord> findAllByOrderBySignedAtDesc(Pageable pageable);

    Page<SignatureRecord> findBySignerNationalIdAndStatusOrderBySignedAtDesc(
            String signerNationalId, SignatureStatus status, Pageable pageable);

    long countBySignedAtAfter(LocalDateTime since);

    long countByStatusAndSignedAtAfter(SignatureStatus status, LocalDateTime since);

    long countByStatus(SignatureStatus status);
}
