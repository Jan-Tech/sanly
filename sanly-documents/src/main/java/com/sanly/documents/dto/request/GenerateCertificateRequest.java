package com.sanly.documents.dto.request;

import com.sanly.documents.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateCertificateRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Source record code is required")
    private String sourceRecordCode;
}
