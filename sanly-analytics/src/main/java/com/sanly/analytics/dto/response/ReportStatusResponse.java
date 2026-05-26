package com.sanly.analytics.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReportStatusResponse(
    String exportCode,
    String status,
    String reportType,
    LocalDate dateFrom,
    LocalDate dateTo,
    LocalDateTime generatedAt,
    LocalDateTime completedAt,
    LocalDateTime expiresAt,
    String failureReason
) {}
