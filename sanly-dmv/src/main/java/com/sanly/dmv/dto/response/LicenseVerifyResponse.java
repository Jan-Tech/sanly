package com.sanly.dmv.dto.response;

import com.sanly.dmv.entity.LicenseCategory;
import com.sanly.dmv.entity.LicenseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Returned by the PUBLIC verify endpoint.
 * Minimal data suitable for police/checkpoint license checks.
 * No PII beyond what the license itself would reveal.
 */
@Data @Builder
public class LicenseVerifyResponse {
    private String licenseNumber;
    private String citizenNationalId;
    private LicenseCategory category;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LicenseStatus status;
    private boolean valid;   // true only when status == ACTIVE and not expired
}
