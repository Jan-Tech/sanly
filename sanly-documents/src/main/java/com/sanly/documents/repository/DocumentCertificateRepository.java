package com.sanly.documents.repository;

import com.sanly.documents.entity.CertificateStatus;
import com.sanly.documents.entity.DocumentCertificate;
import com.sanly.documents.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentCertificateRepository extends JpaRepository<DocumentCertificate, UUID> {

    Optional<DocumentCertificate> findByCertificateCode(String certificateCode);

    List<DocumentCertificate> findByCitizenNationalIdOrderByIssuedAtDesc(String citizenNationalId);

    long countByDocumentTypeAndIssuedAtAfter(DocumentType documentType, LocalDateTime after);

    long countByStatus(CertificateStatus status);

    Page<DocumentCertificate> findAllByOrderByIssuedAtDesc(Pageable pageable);
}
