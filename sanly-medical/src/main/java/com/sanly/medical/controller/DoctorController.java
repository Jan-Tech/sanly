package com.sanly.medical.controller;

import com.sanly.medical.dto.request.DoctorCreateRequest;
import com.sanly.medical.dto.request.DoctorStatusRequest;
import com.sanly.medical.dto.response.ApiResponse;
import com.sanly.medical.dto.response.DoctorResponse;
import com.sanly.medical.service.DoctorService;
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
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Doctors", description = "Doctor account management — Admin only")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a doctor account (linked to a clinic)")
    public ApiResponse<DoctorResponse> register(@Valid @RequestBody DoctorCreateRequest request) {
        return ApiResponse.<DoctorResponse>builder()
                .success(true).message("Doctor registered successfully")
                .data(doctorService.register(request)).build();
    }

    @GetMapping
    @Operation(summary = "List all registered doctors")
    public ApiResponse<List<DoctorResponse>> listAll() {
        return ApiResponse.<List<DoctorResponse>>builder()
                .success(true).data(doctorService.listAll()).build();
    }

    @PatchMapping("/{doctorId}/status")
    @Operation(summary = "Activate or suspend a doctor account")
    public ApiResponse<DoctorResponse> updateStatus(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorStatusRequest request) {
        return ApiResponse.<DoctorResponse>builder()
                .success(true)
                .message("Status updated to " + request.getStatus())
                .data(doctorService.updateStatus(doctorId, request))
                .build();
    }
}
