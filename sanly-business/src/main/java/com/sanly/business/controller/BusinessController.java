package com.sanly.business.controller;

import com.sanly.business.dto.ApiResponse;
import com.sanly.business.dto.BusinessResponse;
import com.sanly.business.entity.BusinessStatus;
import com.sanly.business.service.BusinessServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses")
public class BusinessController {
    private final BusinessServiceImpl businessService;

    public BusinessController(BusinessServiceImpl businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<Page<BusinessResponse>>> findAll(
            @RequestParam(required = false) BusinessStatus status, Pageable pageable) {
        Page<BusinessResponse> page = status != null
                ? businessService.findByStatus(status, pageable)
                : businessService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<BusinessResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(businessService.findById(id)));
    }

    @GetMapping("/by-registration/{registrationNumber}")
    public ResponseEntity<ApiResponse<BusinessResponse>> findByRegistrationNumber(
            @PathVariable String registrationNumber) {
        return ResponseEntity.ok(ApiResponse.ok(businessService.findByRegistrationNumber(registrationNumber)));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessResponse>> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Business suspended", businessService.suspend(id)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Business activated", businessService.activate(id)));
    }

    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusinessResponse>> revoke(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Business revoked", businessService.revoke(id)));
    }
}
