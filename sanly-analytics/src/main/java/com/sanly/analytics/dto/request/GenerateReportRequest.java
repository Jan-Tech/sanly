package com.sanly.analytics.dto.request;

import com.sanly.analytics.entity.ReportType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateReportRequest(
    @NotNull ReportType reportType,
    @NotNull LocalDate dateFrom,
    @NotNull LocalDate dateTo
) {}
