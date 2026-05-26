package com.sanly.bridge.controller;

import com.sanly.bridge.dto.response.ApiResponse;
import com.sanly.bridge.dto.response.ExchangeLogResponse;
import com.sanly.bridge.dto.response.PageResponse;
import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.ExchangeResult;
import com.sanly.bridge.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit", description = "Exchange log inspection — Admin only")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final ExchangeService exchangeService;

    @GetMapping("/exchanges")
    @Operation(summary = "Full exchange log with optional filters")
    public ApiResponse<PageResponse<ExchangeLogResponse>> getAllExchanges(
            @RequestParam(required = false) String institutionCode,
            @RequestParam(required = false) String nationalId,
            @RequestParam(required = false) DataType dataType,
            @RequestParam(required = false) ExchangeResult result,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        PageResponse<ExchangeLogResponse> body = exchangeService
                .getAuditLog(institutionCode, nationalId, dataType, result, from, to, page, size);

        return ApiResponse.<PageResponse<ExchangeLogResponse>>builder()
                .success(true)
                .data(body)
                .build();
    }

    @GetMapping("/exchanges/{nationalId}")
    @Operation(summary = "All exchanges involving a specific citizen")
    public ApiResponse<PageResponse<ExchangeLogResponse>> getByNationalId(
            @PathVariable String nationalId,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ApiResponse.<PageResponse<ExchangeLogResponse>>builder()
                .success(true)
                .data(exchangeService.getAuditLogByNationalId(nationalId, page, size))
                .build();
    }
}
