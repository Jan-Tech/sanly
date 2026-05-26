package com.sanly.court.repository;

import com.sanly.court.entity.CourtDocument;
import com.sanly.court.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourtDocumentRepository extends JpaRepository<CourtDocument, UUID> {
    Optional<CourtDocument> findByDocumentCode(String documentCode);
    List<CourtDocument> findByCaseNumber(String caseNumber);
    List<CourtDocument> findByStatus(DocumentStatus status);
}
