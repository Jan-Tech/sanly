package com.sanly.vehicle.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransferApplicationResponse(
        Long applicationId,
        String plateNumber,
        String fromNationalId,
        String toNationalId,
        String toBusinessNumber,
        String transferType,
        String agreedPrice,
        LocalDate applicationDate,
        String status,
        Long processedByOfficerId,
        LocalDateTime processedAt,
        String rejectionReason
) {}
