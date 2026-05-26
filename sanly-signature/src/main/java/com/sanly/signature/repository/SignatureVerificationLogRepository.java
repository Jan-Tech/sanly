package com.sanly.signature.repository;

import com.sanly.signature.entity.SignatureVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SignatureVerificationLogRepository extends JpaRepository<SignatureVerificationLog, UUID> {

    List<SignatureVerificationLog> findBySignatureCodeOrderByVerifiedAtDesc(String signatureCode);

    long countByVerifiedAtAfter(LocalDateTime since);

    long countBySignatureCodeAndVerifiedAtAfter(String signatureCode, LocalDateTime since);
}
