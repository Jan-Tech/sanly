package com.sanly.civil.controller;

import com.sanly.civil.dto.ApiResponse;
import com.sanly.civil.dto.CreateOfficerRequest;
import com.sanly.civil.dto.OfficerResponse;
import com.sanly.civil.service.OfficerServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/officers")
@PreAuthorize("hasRole('ADMIN')")
public class OfficerController {
    private final OfficerServiceImpl officerService;

    public OfficerController(OfficerServiceImpl officerService) {
        this.officerService = officerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OfficerResponse>> create(@Valid @RequestBody CreateOfficerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Officer created", officerService.create(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OfficerResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(officerService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfficerResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(officerService.findById(id)));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<OfficerResponse>> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Officer suspended", officerService.suspend(id)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<OfficerResponse>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Officer activated", officerService.activate(id)));
    }
}
