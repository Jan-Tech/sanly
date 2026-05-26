package com.sanly.medical.controller;

import com.sanly.medical.dto.request.ClinicCreateRequest;
import com.sanly.medical.dto.request.ClinicStatusRequest;
import com.sanly.medical.dto.response.ApiResponse;
import com.sanly.medical.dto.response.ClinicResponse;
import com.sanly.medical.service.ClinicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clinics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Clinics", description = "Clinic management — Admin only")
@SecurityRequirement(name = "bearerAuth")
public class ClinicController {

    private final ClinicService clinicService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new clinic")
    public ApiResponse<ClinicResponse> register(@Valid @RequestBody ClinicCreateRequest request) {
        return ApiResponse.<ClinicResponse>builder()
                .success(true).message("Clinic registered successfully")
                .data(clinicService.register(request)).build();
    }

    @GetMapping
    @Operation(summary = "List all registered clinics")
    public ApiResponse<List<ClinicResponse>> listAll() {
        return ApiResponse.<List<ClinicResponse>>builder()
                .success(true).data(clinicService.listAll()).build();
    }

    @PatchMapping("/{clinicId}/status")
    @Operation(summary = "Activate or suspend a clinic")
    public ApiResponse<ClinicResponse> updateStatus(
            @PathVariable Long clinicId,
            @Valid @RequestBody ClinicStatusRequest request) {
        return ApiResponse.<ClinicResponse>builder()
                .success(true)
                .message("Status updated to " + request.getStatus())
                .data(clinicService.updateStatus(clinicId, request))
                .build();
    }
}
