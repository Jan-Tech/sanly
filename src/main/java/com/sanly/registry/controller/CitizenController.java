package com.sanly.registry.controller;

import com.sanly.registry.audit.AuditService;
import com.sanly.registry.dto.request.CitizenCreateRequest;
import com.sanly.registry.dto.request.CitizenUpdateRequest;
import com.sanly.registry.dto.request.PhoneUpdateRequest;
import com.sanly.registry.dto.request.StatusUpdateRequest;
import com.sanly.registry.dto.response.ApiResponse;
import com.sanly.registry.dto.response.CitizenResponse;
import com.sanly.registry.dto.response.CitizenVerifyResponse;
import com.sanly.registry.dto.response.PageResponse;
import com.sanly.registry.service.CitizenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/citizens")
@RequiredArgsConstructor
@Tag(name = "Citizens", description = "Citizen Registry — CRUD and verification endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CitizenController {

    private static final String NIN_PATTERN = "\\d{11}";
    private static final String NIN_MSG     = "National ID must be exactly 11 digits";

    private final CitizenService citizenService;
    private final AuditService   auditService;

    // ===== POST /api/v1/citizens =====

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new citizen (auto-generates TM-NIN)")
    public ApiResponse<CitizenResponse> register(
            @Valid @RequestBody CitizenCreateRequest request,
            HttpServletRequest httpRequest) {

        CitizenResponse body = citizenService.register(request);
        auditService.log("CITIZEN_CREATED", body.getNationalId(), httpRequest);

        return ApiResponse.<CitizenResponse>builder()
                .success(true)
                .message("Citizen registered. National ID: " + body.getNationalId())
                .data(body)
                .build();
    }

    // ===== GET /api/v1/citizens/{nationalId} =====

    @GetMapping("/{nationalId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTITUTION') "
            + "or (hasRole('CITIZEN') and @securityService.isOwnRecord(#nationalId))")
    @Operation(summary = "Retrieve full citizen record by National ID")
    public ApiResponse<CitizenResponse> getByNationalId(
            @PathVariable @Pattern(regexp = NIN_PATTERN, message = NIN_MSG) String nationalId,
            HttpServletRequest httpRequest) {

        CitizenResponse body = citizenService.getByNationalId(nationalId);
        auditService.log("CITIZEN_READ", nationalId, httpRequest);

        return ApiResponse.<CitizenResponse>builder()
                .success(true)
                .data(body)
                .build();
    }

    // ===== GET /api/v1/citizens/{nationalId}/verify =====

    @GetMapping("/{nationalId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','INSTITUTION','CITIZEN')")
    @Operation(summary = "Verify a citizen exists and is ACTIVE (lightweight check)")
    public ApiResponse<CitizenVerifyResponse> verify(
            @PathVariable @Pattern(regexp = NIN_PATTERN, message = NIN_MSG) String nationalId,
            HttpServletRequest httpRequest) {

        CitizenVerifyResponse body = citizenService.verify(nationalId);
        auditService.log("CITIZEN_VERIFIED", nationalId, httpRequest);

        return ApiResponse.<CitizenVerifyResponse>builder()
                .success(true)
                .data(body)
                .build();
    }

    // ===== PUT /api/v1/citizens/{nationalId} =====

    @PutMapping("/{nationalId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a citizen's mutable fields")
    public ApiResponse<CitizenResponse> update(
            @PathVariable @Pattern(regexp = NIN_PATTERN, message = NIN_MSG) String nationalId,
            @Valid @RequestBody CitizenUpdateRequest request,
            HttpServletRequest httpRequest) {

        CitizenResponse body = citizenService.update(nationalId, request);
        auditService.log("CITIZEN_UPDATED", nationalId, httpRequest);

        return ApiResponse.<CitizenResponse>builder()
                .success(true)
                .message("Citizen record updated")
                .data(body)
                .build();
    }

    // ===== PATCH /api/v1/citizens/{nationalId}/status =====

    @PatchMapping("/{nationalId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change citizen status (ACTIVE / DECEASED / SUSPENDED)")
    public ApiResponse<CitizenResponse> updateStatus(
            @PathVariable @Pattern(regexp = NIN_PATTERN, message = NIN_MSG) String nationalId,
            @Valid @RequestBody StatusUpdateRequest request,
            HttpServletRequest httpRequest) {

        CitizenResponse body = citizenService.updateStatus(nationalId, request);
        auditService.log("CITIZEN_STATUS_CHANGED", nationalId, httpRequest);

        return ApiResponse.<CitizenResponse>builder()
                .success(true)
                .message("Status updated to " + request.getStatus())
                .data(body)
                .build();
    }

    // ===== PATCH /api/v1/citizens/{nationalId}/phone =====

    @PatchMapping("/{nationalId}/phone")
    @PreAuthorize("hasRole('ADMIN') "
            + "or (hasRole('CITIZEN') and @securityService.isOwnRecord(#nationalId))")
    @Operation(summary = "Update a citizen's registered phone number (E.164 format)")
    public ApiResponse<CitizenResponse> updatePhone(
            @PathVariable @Pattern(regexp = NIN_PATTERN, message = NIN_MSG) String nationalId,
            @Valid @RequestBody PhoneUpdateRequest request,
            HttpServletRequest httpRequest) {

        CitizenResponse body = citizenService.updatePhone(nationalId, request.getPhoneNumber());
        auditService.log("CITIZEN_PHONE_UPDATED", nationalId, httpRequest);

        return ApiResponse.<CitizenResponse>builder()
                .success(true)
                .message("Phone number updated.")
                .data(body)
                .build();
    }

    // ===== GET /api/v1/citizens/search =====

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','INSTITUTION')")
    @Operation(summary = "Search citizens by name, date of birth, and/or region")
    public ApiResponse<PageResponse<CitizenResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            HttpServletRequest httpRequest) {

        PageResponse<CitizenResponse> body = citizenService.search(name, dob, region, page, size);
        auditService.log("CITIZEN_SEARCH", null, httpRequest);

        return ApiResponse.<PageResponse<CitizenResponse>>builder()
                .success(true)
                .data(body)
                .build();
    }
}
