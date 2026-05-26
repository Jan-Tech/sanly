package com.sanly.dmv.controller;

import com.sanly.dmv.dto.request.IssueLicenseRequest;
import com.sanly.dmv.dto.request.LicenseStatusRequest;
import com.sanly.dmv.dto.response.ApiResponse;
import com.sanly.dmv.dto.response.DrivingLicenseResponse;
import com.sanly.dmv.dto.response.LicenseVerifyResponse;
import com.sanly.dmv.dto.response.PageResponse;
import com.sanly.dmv.service.DrivingLicenseServiceImpl;
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
@RequestMapping("/api/v1/licenses")
@RequiredArgsConstructor
@Tag(name = "Driving Licenses", description = "Issue and manage driving licenses")
public class DrivingLicenseController {

    private final DrivingLicenseServiceImpl licenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Issue a driving license (checks vision test from SANLY Bridge)")
    public ApiResponse<DrivingLicenseResponse> issueLicense(
            @Valid @RequestBody IssueLicenseRequest request) {
        DrivingLicenseResponse body = licenseService.issueLicense(request);
        return ApiResponse.<DrivingLicenseResponse>builder()
                .success(true)
                .message("License issued: " + body.getLicenseNumber())
                .data(body)
                .build();
    }

    @GetMapping("/{licenseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get license details by UUID")
    public ApiResponse<DrivingLicenseResponse> getById(@PathVariable UUID licenseId) {
        return ApiResponse.<DrivingLicenseResponse>builder()
                .success(true).data(licenseService.getById(licenseId)).build();
    }

    @GetMapping("/citizen/{nationalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "All licenses for a citizen")
    public ApiResponse<PageResponse<DrivingLicenseResponse>> getByNationalId(
            @PathVariable String nationalId,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.<PageResponse<DrivingLicenseResponse>>builder()
                .success(true)
                .data(licenseService.getByNationalId(nationalId, page, size))
                .build();
    }

    /**
     * PUBLIC endpoint — no authentication required.
     * Used by police and checkpoints to verify a license number in real time.
     */
    @GetMapping("/verify/{licenseNumber}")
    @Operation(summary = "PUBLIC — Verify a license number (police/checkpoint scan, no auth needed)")
    public ApiResponse<LicenseVerifyResponse> verify(@PathVariable String licenseNumber) {
        return ApiResponse.<LicenseVerifyResponse>builder()
                .success(true)
                .data(licenseService.verify(licenseNumber))
                .build();
    }

    @PatchMapping("/{licenseId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Suspend or revoke a license")
    public ApiResponse<DrivingLicenseResponse> updateStatus(
            @PathVariable UUID licenseId,
            @Valid @RequestBody LicenseStatusRequest request) {
        return ApiResponse.<DrivingLicenseResponse>builder()
                .success(true)
                .message("License status updated to " + request.getStatus())
                .data(licenseService.updateStatus(licenseId, request))
                .build();
    }
}
