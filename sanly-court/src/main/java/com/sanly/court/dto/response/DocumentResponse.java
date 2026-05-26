package com.sanly.court.dto.response;

import com.sanly.court.entity.CourtDocument;
import com.sanly.court.entity.DocumentStatus;
import com.sanly.court.entity.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID documentId, String documentCode, String caseNumber, DocumentType documentType,
        String title, String content, String submittedByNationalId,
        LocalDateTime submittedAt, DocumentStatus status, String digitalSignature, LocalDateTime createdAt
) {
    public static DocumentResponse from(CourtDocument d) {
        return new DocumentResponse(d.getDocumentId(), d.getDocumentCode(), d.getCaseNumber(),
                d.getDocumentType(), d.getTitle(), d.getContent(), d.getSubmittedByNationalId(),
                d.getSubmittedAt(), d.getStatus(), d.getDigitalSignature(), d.getCreatedAt());
    }
}
