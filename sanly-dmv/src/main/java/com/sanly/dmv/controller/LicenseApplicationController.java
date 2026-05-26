package com.sanly.dmv.controller;

import com.sanly.dmv.dto.request.LicenseApplicationRequest;
import com.sanly.dmv.dto.request.ProcessApplicationRequest;
import com.sanly.dmv.dto.response.ApiResponse;
import com.sanly.dmv.dto.response.LicenseApplicationResponse;
import com.sanly.dmv.dto.response.PageResponse;
import com.sanly.dmv.service.LicenseApplicationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
@Tag(name = "License Applications", description = "Apply for and process driving license applications")
@SecurityRequirement(name = "bearerAuth")
public class LicenseApplicationController {

    private final LicenseApplicationServiceImpl applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a license application on behalf of a citizen")
    public ApiResponse<LicenseApplicationResponse> create(
            @Valid @RequestBody LicenseApplicationRequest request) {
        return ApiResponse.<LicenseApplicationResponse>builder()
                .success(true).message("Application submitted")
                .data(applicationService.create(request)).build();
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get application details by ID")
    public ApiResponse<LicenseApplicationResponse> getById(@PathVariable UUID applicationId) {
        return ApiResponse.<LicenseApplicationResponse>builder()
                .success(true).data(applicationService.getById(applicationId)).build();
    }

    @GetMapping("/citizen/{nationalId}")
    @Operation(summary = "All applications for a citizen")
    public ApiResponse<PageResponse<LicenseApplicationResponse>> getByNationalId(
            @PathVariable String nationalId,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.<PageResponse<LicenseApplicationResponse>>builder()
                .success(true)
                .data(applicationService.getByNationalId(nationalId, page, size))
                .build();
    }

    @PostMapping("/{applicationId}/process")
    @Operation(summary = "Approve or reject an application")
    public ApiResponse<LicenseApplicationResponse> process(
            @PathVariable UUID applicationId,
            @Valid @RequestBody ProcessApplicationRequest request) {
        return ApiResponse.<LicenseApplicationResponse>builder()
                .success(true)
                .message("Application " + request.getStatus().name().toLowerCase())
                .data(applicationService.process(applicationId, request))
                .build();
    }
}
