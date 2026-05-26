package com.sanly.dmv.dto.response;

import com.sanly.dmv.entity.ApplicationStatus;
import com.sanly.dmv.entity.LicenseCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class LicenseApplicationResponse {
    private UUID applicationId;
    private String citizenNationalId;
    private LicenseCategory requestedCategory;
    private LocalDateTime appliedAt;
    private ApplicationStatus status;
    private String rejectionReason;
    private Long processedByOfficerId;
    private LocalDateTime processedAt;
}
