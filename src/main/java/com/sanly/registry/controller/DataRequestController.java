package com.sanly.registry.controller;

import com.sanly.registry.audit.AuditService;
import com.sanly.registry.config.UserDetailsImpl;
import com.sanly.registry.dto.request.DataRequestSubmitRequest;
import com.sanly.registry.dto.request.ReviewActionRequest;
import com.sanly.registry.dto.response.ApiResponse;
import com.sanly.registry.dto.response.DataRequestResponse;
import com.sanly.registry.dto.response.PageResponse;
import com.sanly.registry.entity.AffectedService;
import com.sanly.registry.entity.RequestStatus;
import com.sanly.registry.entity.RequestType;
import com.sanly.registry.service.DataRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/data-requests")
@RequiredArgsConstructor
public class DataRequestController {

    private final DataRequestService dataRequestService;
    private final AuditService auditService;

    // ===== CITIZEN endpoints =====

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CITIZEN')")
    public ApiResponse<DataRequestResponse> submit(
            @Valid @RequestBody DataRequestSubmitRequest request,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {

        String nationalId = extractNationalId(principal);
        DataRequestResponse body = dataRequestService.submit(nationalId, request);
        auditService.log("DATA_REQUEST_SUBMITTED", nationalId, httpRequest);

        return ApiResponse.<DataRequestResponse>builder()
                .success(true)
                .message("Data request submitted successfully. Request code: " + body.getRequestCode())
                .data(body)
                .build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApiResponse<PageResponse<DataRequestResponse>> getMyRequests(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {

        String nationalId = extractNationalId(principal);
        PageResponse<DataRequestResponse> body = dataRequestService.getMyRequests(nationalId, page, size);
        auditService.log("DATA_REQUEST_LIST", nationalId, httpRequest);

        return ApiResponse.<PageResponse<DataRequestResponse>>builder()
                .success(true)
                .data(body)
                .build();
    }

    @GetMapping("/my/{requestCode}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApiResponse<DataRequestResponse> getMyRequest(
            @PathVariable String requestCode,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {

        String nationalId = extractNationalId(principal);
        DataRequestResponse body = dataRequestService.getMyRequest(nationalId, requestCode);
        auditService.log("DATA_REQUEST_READ", nationalId, httpRequest);

        return ApiResponse.<DataRequestResponse>builder()
                .success(true)
                .data(body)
                .build();
    }

    @GetMapping("/my/{requestCode}/export")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<String> downloadExport(
            @PathVariable String requestCode,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest httpRequest) {

        String nationalId = extractNationalId(principal);
        String exportJson = dataRequestService.getExportData(nationalId, requestCode);
        auditService.log("DATA_EXPORT_DOWNLOADED", nationalId, httpRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"data-export-" + requestCode + ".json\"");

        return ResponseEntity.ok().headers(headers).body(exportJson);
    }

    // ===== ADMIN endpoints =====

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<DataRequestResponse>> adminGetAll(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestType requestType,
            @RequestParam(required = false) AffectedService affectedService,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest httpRequest) {

        PageResponse<DataRequestResponse> body = dataRequestService.adminGetAll(
                status, requestType, affectedService, page, size);
        auditService.log("DATA_REQUEST_ADMIN_LIST", null, httpRequest);

        return ApiResponse.<PageResponse<DataRequestResponse>>builder()
                .success(true)
                .data(body)
                .build();
    }

    @GetMapping("/{requestCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DataRequestResponse> adminGetByCode(
            @PathVariable String requestCode,
            HttpServletRequest httpRequest) {

        DataRequestResponse body = dataRequestService.adminGetByCode(requestCode);
        auditService.log("DATA_REQUEST_ADMIN_READ", body.getCitizenNationalId(), httpRequest);

        return ApiResponse.<DataRequestResponse>builder()
                .success(true)
                .data(body)
                .build();
    }

    @PatchMapping("/{requestCode}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DataRequestResponse> startReview(
            @PathVariable String requestCode,
            HttpServletRequest httpRequest) {

        String officerId = resolveOfficerId();
        DataRequestResponse body = dataRequestService.startReview(requestCode, officerId);
        auditService.log("DATA_REQUEST_REVIEW_STARTED", body.getCitizenNationalId(), httpRequest);

        return ApiResponse.<DataRequestResponse>builder()
                .success(true)
                .message("Request is now under review.")
                .data(body)
                .build();
    }

    @PatchMapping("/{requestCode}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DataRequestResponse> approve(
            @PathVariable String requestCode,
            @Valid @RequestBody ReviewActionRequest reviewAction,
            HttpServletRequest httpRequest) {

        String officerId = resolveOfficerId();
        DataRequestResponse body = dataRequestService.approve(requestCode, officerId, reviewAction.getNotes());
        auditService.log("DATA_REQUEST_APPROVED", body.getCitizenNationalId(), httpRequest);

        return ApiResponse.<DataRequestResponse>builder()
                .success(true)
                .message("Request approved.")
                .data(body)
                .build();
    }

    @PatchMapping("/{requestCode}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DataRequestResponse> reject(
            @PathVariable String requestCode,
            @Valid @RequestBody ReviewActionRequest reviewAction,
            HttpServletRequest httpRequest) {

        String officerId = resolveOfficerId();
        DataRequestResponse body = dataRequestService.reject(requestCode, officerId, reviewAction.getNotes());
        auditService.log("DATA_REQUEST_REJECTED", body.getCitizenNationalId(), httpRequest);

        return ApiResponse.<DataRequestResponse>builder()
                .success(true)
                .message("Request rejected.")
                .data(body)
                .build();
    }

    @PatchMapping("/{requestCode}/partial")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DataRequestResponse> partialApprove(
            @PathVariable String requestCode,
            @Valid @RequestBody ReviewActionRequest reviewAction,
            HttpServletRequest httpRequest) {

        String officerId = resolveOfficerId();
        DataRequestResponse body = dataRequestService.partialApprove(requestCode, officerId, reviewAction.getNotes());
        auditService.log("DATA_REQUEST_PARTIAL_APPROVED", body.getCitizenNationalId(), httpRequest);

        return ApiResponse.<DataRequestResponse>builder()
                .success(true)
                .message("Request partially approved.")
                .data(body)
                .build();
    }

    // ---- private helpers ----

    private String extractNationalId(Object principal) {
        if (principal instanceof UserDetailsImpl ud) {
            return ud.getNationalId();
        }
        // Citizen JWT encodes the nationalId directly as the principal string
        if (principal instanceof String s) {
            return s;
        }
        // Fallback: read from SecurityContext authentication name
        Object authPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authPrincipal instanceof String s) {
            return s;
        }
        if (authPrincipal instanceof UserDetailsImpl ud) {
            return ud.getNationalId();
        }
        throw new IllegalStateException("Cannot resolve national ID from security principal");
    }

    private String resolveOfficerId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl ud) {
            return ud.getUsername();
        }
        if (principal instanceof String s) {
            return s;
        }
        return "unknown";
    }
}
