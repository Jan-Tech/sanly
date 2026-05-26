package com.sanly.civil.controller;

import com.sanly.civil.dto.ApiResponse;
import com.sanly.civil.dto.DeathRecordResponse;
import com.sanly.civil.dto.RegisterDeathRequest;
import com.sanly.civil.service.DeathRecordServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/death-records")
@PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
public class DeathRecordController {
    private final DeathRecordServiceImpl deathRecordService;

    public DeathRecordController(DeathRecordServiceImpl deathRecordService) {
        this.deathRecordService = deathRecordService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeathRecordResponse>> register(
            @Valid @RequestBody RegisterDeathRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Death record registered", deathRecordService.register(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeathRecordResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(deathRecordService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeathRecordResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(deathRecordService.findById(id)));
    }

    @GetMapping("/by-national-id/{nationalId}")
    public ResponseEntity<ApiResponse<DeathRecordResponse>> findByNationalId(@PathVariable String nationalId) {
        return ResponseEntity.ok(ApiResponse.ok(deathRecordService.findByNationalId(nationalId)));
    }
}
