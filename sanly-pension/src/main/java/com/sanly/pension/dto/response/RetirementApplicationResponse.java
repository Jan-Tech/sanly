package com.sanly.pension.dto.response;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record RetirementApplicationResponse(
        Long applicationId, String accountCode, String citizenNationalId,
        LocalDateTime appliedAt, LocalDate requestedStartDate, String status,
        Long processedByOfficerId, LocalDateTime processedAt,
        String rejectionReason, String monthlyPensionAmount) {}
