package com.sanly.bridge.controller;

import com.sanly.bridge.dto.request.ReviewAnomalyRequest;
import com.sanly.bridge.dto.response.AnomalyAlertResponse;
import com.sanly.bridge.dto.response.AnomalyAlertSummaryResponse;
import com.sanly.bridge.dto.response.ApiResponse;
import com.sanly.bridge.dto.response.PageResponse;
import com.sanly.bridge.entity.AlertSeverity;
import com.sanly.bridge.entity.AlertStatus;
import com.sanly.bridge.entity.AnomalyAlert;
import com.sanly.bridge.repository.AnomalyAlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/anomalies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Anomaly Detection",
     description = "Anti-corruption anomaly alert management — Admin only")
public class AnomalyController {

    private final AnomalyAlertRepository anomalyAlertRepository;

    @GetMapping
    @Operation(summary = "List anomaly alerts with optional filters")
    public ApiResponse<PageResponse<AnomalyAlertResponse>> listAll(
            @RequestParam(required = false) String institutionCode,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        Page<AnomalyAlert> pg = anomalyAlertRepository.findWithFilters(
                institutionCode, severity, status, from, to, PageRequest.of(page, size));

        return ApiResponse.<PageResponse<AnomalyAlertResponse>>builder()
                .success(true)
                .data(PageResponse.<AnomalyAlertResponse>builder()
                        .content(pg.getContent().stream().map(AnomalyAlertResponse::from).toList())
                        .page(pg.getNumber()).size(pg.getSize())
                        .totalElements(pg.getTotalElements()).totalPages(pg.getTotalPages())
                        .last(pg.isLast()).build())
                .build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Count of OPEN alerts by severity — for dashboard widgets")
    public ApiResponse<AnomalyAlertSummaryResponse> summary() {
        long totalOpen = anomalyAlertRepository.countByStatus(AlertStatus.OPEN);
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (AlertSeverity sev : AlertSeverity.values()) {
            bySeverity.put(sev.name(),
                    anomalyAlertRepository.countByStatusAndSeverity(AlertStatus.OPEN, sev));
        }
        return ApiResponse.<AnomalyAlertSummaryResponse>builder()
                .success(true)
                .data(AnomalyAlertSummaryResponse.builder()
                        .totalOpen(totalOpen)
                        .openBySeverity(bySeverity)
                        .build())
                .build();
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "Get anomaly alert details by ID")
    public ApiResponse<AnomalyAlertResponse> getById(@PathVariable UUID alertId) {
        AnomalyAlert alert = anomalyAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Alert not found: " + alertId));
        return ApiResponse.<AnomalyAlertResponse>builder()
                .success(true)
                .data(AnomalyAlertResponse.from(alert))
                .build();
    }

    @PatchMapping("/{alertId}/review")
    @Operation(summary = "Mark an alert as REVIEWED or DISMISSED")
    public ApiResponse<AnomalyAlertResponse> review(
            @PathVariable UUID alertId,
            @Valid @RequestBody ReviewAnomalyRequest req) {

        AnomalyAlert alert = anomalyAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Alert not found: " + alertId));

        if (alert.getStatus() != AlertStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Alert is already " + alert.getStatus());
        }
        if (req.getStatus() == AlertStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot set status back to OPEN");
        }

        alert.setStatus(req.getStatus());
        alert.setReviewedBy(req.getReviewedBy());
        alert.setReviewedAt(LocalDateTime.now());
        AnomalyAlert saved = anomalyAlertRepository.save(alert);

        return ApiResponse.<AnomalyAlertResponse>builder()
                .success(true)
                .message("Alert marked as " + req.getStatus())
                .data(AnomalyAlertResponse.from(saved))
                .build();
    }
}
