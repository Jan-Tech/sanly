package com.sanly.civil.controller;

import com.sanly.civil.dto.ApiResponse;
import com.sanly.civil.dto.MarriageRecordResponse;
import com.sanly.civil.dto.RegisterMarriageRequest;
import com.sanly.civil.service.MarriageRecordServiceImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marriage-records")
@PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
public class MarriageRecordController {
    private final MarriageRecordServiceImpl marriageRecordService;

    public MarriageRecordController(MarriageRecordServiceImpl marriageRecordService) {
        this.marriageRecordService = marriageRecordService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MarriageRecordResponse>> register(
            @Valid @RequestBody RegisterMarriageRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Marriage record registered", marriageRecordService.register(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MarriageRecordResponse>>> findAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(marriageRecordService.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarriageRecordResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(marriageRecordService.findById(id)));
    }

    @PostMapping("/{id}/dissolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MarriageRecordResponse>> dissolve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Marriage dissolved", marriageRecordService.dissolve(id)));
    }
}
