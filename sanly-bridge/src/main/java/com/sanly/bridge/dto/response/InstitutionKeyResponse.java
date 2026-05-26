package com.sanly.bridge.dto.response;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.InstitutionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Returned ONLY when an institution is first registered or its API key is rotated.
 * The {@code apiKey} field contains the raw plaintext key — this is the ONLY time
 * it is ever returned. It is not stored in the database.
 * Instruct the institution to store it securely immediately.
 */
@Data
@Builder
public class InstitutionKeyResponse {
    private String institutionCode;
    private String name;
    private Set<DataType> publishableTypes;
    private InstitutionStatus status;
    /** Raw plaintext API key — shown once. Store securely. */
    private String apiKey;
    private LocalDateTime createdAt;
}
