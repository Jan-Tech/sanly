package com.sanly.dmv.dto.response;

import com.sanly.dmv.entity.LicenseCategory;
import com.sanly.dmv.entity.LicenseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class DrivingLicenseResponse {
    private UUID licenseId;
    private String citizenNationalId;
    private String licenseNumber;
    private LicenseCategory category;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private Long issuedByOfficerId;
    private LicenseStatus status;
    private String visionTestRef;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
