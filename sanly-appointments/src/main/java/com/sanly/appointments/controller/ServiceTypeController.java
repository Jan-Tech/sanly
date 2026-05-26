package com.sanly.appointments.controller;

import com.sanly.appointments.dto.request.CreateServiceTypeRequest;
import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.entity.ServiceStatus;
import com.sanly.appointments.entity.ServiceType;
import com.sanly.appointments.service.ServiceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments/services")
@RequiredArgsConstructor
@Tag(name = "Service Types", description = "Services offered at each office")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a service type to an office")
    public ApiResponse<ServiceType> createServiceType(@Valid @RequestBody CreateServiceTypeRequest req) {
        return ApiResponse.<ServiceType>builder().success(true)
                .data(serviceTypeService.createServiceType(req)).build();
    }

    @GetMapping("/office/{officeCode}")
    @Operation(summary = "Get all active service types for an office — public")
    public ApiResponse<List<ServiceType>> getByOffice(@PathVariable String officeCode) {
        return ApiResponse.<List<ServiceType>>builder().success(true)
                .data(serviceTypeService.getByOffice(officeCode)).build();
    }

    @PatchMapping("/{serviceTypeId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update service type status")
    public ApiResponse<ServiceType> updateStatus(@PathVariable UUID serviceTypeId,
                                                  @RequestParam ServiceStatus status) {
        return ApiResponse.<ServiceType>builder().success(true)
                .data(serviceTypeService.updateStatus(serviceTypeId, status)).build();
    }
}
