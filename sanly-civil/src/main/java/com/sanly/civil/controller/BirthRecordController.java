package com.sanly.civil.controller;

import com.sanly.civil.dto.ApiResponse;
import com.sanly.civil.dto.BirthRecordResponse;
import com.sanly.civil.dto.RegisterBirthRequest;
import com.sanly.civil.service.BirthRecordServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/birth-records")
@PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
public class BirthRecordController {
    private final BirthRecordServiceImpl birthRecordService;

    public BirthRecordController(BirthRecordServiceImpl birthRecordService) {
        this.birthRecordService = birthRecordService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BirthRecordResponse>> register(
            @Valid @RequestBody RegisterBirthRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Birth record registered", birthRecordService.register(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BirthRecordResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(birthRecordService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BirthRecordResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(birthRecordService.findById(id)));
    }

    @GetMapping("/by-national-id/{nationalId}")
    public ResponseEntity<ApiResponse<BirthRecordResponse>> findByNationalId(@PathVariable String nationalId) {
        return ResponseEntity.ok(ApiResponse.ok(birthRecordService.findByNationalId(nationalId)));
    }
}
