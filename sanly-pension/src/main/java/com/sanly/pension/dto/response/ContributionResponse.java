package com.sanly.pension.dto.response;
import java.time.LocalDateTime;
public record ContributionResponse(
        Long contributionId, String accountCode, String employerCode,
        String contributionMonth, String employerAmount, String citizenAmount,
        String totalAmount, LocalDateTime submittedAt, Long submittedByOfficerId,
        String status, String rejectionReason) {}
