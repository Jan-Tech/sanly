package com.sanly.court.dto.request;
import com.sanly.court.entity.DocumentType;
public record SubmitDocumentRequest(
        DocumentType documentType,
        String title,
        String content,
        String submittedByNationalId
) {}
