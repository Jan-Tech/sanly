package com.sanly.documents.repository;

import com.sanly.documents.entity.CertificateVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CertificateVerificationLogRepository extends JpaRepository<CertificateVerificationLog, UUID> {

    List<CertificateVerificationLog> findByCertificateCodeOrderByVerifiedAtDesc(String certificateCode);

    long countByVerifiedAtAfter(LocalDateTime after);
}
