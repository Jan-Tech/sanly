package com.sanly.appointments.controller;

import com.sanly.appointments.dto.request.CreateOfficeRequest;
import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.dto.response.OfficeResponse;
import com.sanly.appointments.entity.InstitutionType;
import com.sanly.appointments.entity.OfficeStatus;
import com.sanly.appointments.service.OfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments/offices")
@RequiredArgsConstructor
@Tag(name = "Offices", description = "Government office management")
public class OfficeController {

    private final OfficeService officeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new government office")
    public ApiResponse<OfficeResponse> createOffice(@Valid @RequestBody CreateOfficeRequest req) {
        return ApiResponse.<OfficeResponse>builder().success(true)
                .data(officeService.createOffice(req)).build();
    }

    @GetMapping
    @Operation(summary = "List offices — public, filterable by institutionType and region")
    public ApiResponse<List<OfficeResponse>> listOffices(
            @RequestParam(required = false) InstitutionType institutionType,
            @RequestParam(required = false) String region) {
        return ApiResponse.<List<OfficeResponse>>builder().success(true)
                .data(officeService.listOffices(institutionType, region)).build();
    }

    @GetMapping("/{officeCode}")
    @Operation(summary = "Get office details by code")
    public ApiResponse<OfficeResponse> getOffice(@PathVariable String officeCode) {
        return ApiResponse.<OfficeResponse>builder().success(true)
                .data(officeService.getByCode(officeCode)).build();
    }

    @PatchMapping("/{officeCode}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update office status (ACTIVE / CLOSED / TEMPORARILY_CLOSED)")
    public ApiResponse<OfficeResponse> updateStatus(@PathVariable String officeCode,
                                                     @RequestParam OfficeStatus status) {
        return ApiResponse.<OfficeResponse>builder().success(true)
                .data(officeService.updateStatus(officeCode, status)).build();
    }
}
