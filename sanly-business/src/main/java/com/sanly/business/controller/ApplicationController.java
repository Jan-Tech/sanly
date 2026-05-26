package com.sanly.business.controller;

import com.sanly.business.dto.ApiResponse;
import com.sanly.business.dto.ApplicationResponse;
import com.sanly.business.dto.CreateApplicationRequest;
import com.sanly.business.dto.ProcessApplicationRequest;
import com.sanly.business.entity.ApplicationStatus;
import com.sanly.business.service.ApplicationServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private final ApplicationServiceImpl applicationService;

    public ApplicationController(ApplicationServiceImpl applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> submit(
            @Valid @RequestBody CreateApplicationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application submitted", applicationService.submit(req)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> findAll(
            @RequestParam(required = false) ApplicationStatus status, Pageable pageable) {
        Page<ApplicationResponse> page = status != null
                ? applicationService.findByStatus(status, pageable)
                : applicationService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(applicationService.findById(id)));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> process(
            @PathVariable UUID id, @Valid @RequestBody ProcessApplicationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Application processed", applicationService.process(id, req)));
    }
}
