package com.sanly.medical.controller;

import com.sanly.medical.dto.request.MedicalRecordCreateRequest;
import com.sanly.medical.dto.request.RecordResultUpdateRequest;
import com.sanly.medical.dto.response.ApiResponse;
import com.sanly.medical.dto.response.MedicalRecordResponse;
import com.sanly.medical.dto.response.PageResponse;
import com.sanly.medical.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
@Tag(name = "Medical Records", description = "Submit and manage medical test results")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a new medical test result for a citizen")
    public ApiResponse<MedicalRecordResponse> create(
            @Valid @RequestBody MedicalRecordCreateRequest request) {
        MedicalRecordResponse body = recordService.create(request);
        return ApiResponse.<MedicalRecordResponse>builder()
                .success(true)
                .message("Medical record created and submitted to SANLY Bridge")
                .data(body)
                .build();
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "Get a specific medical record by ID")
    public ApiResponse<MedicalRecordResponse> getById(@PathVariable UUID recordId) {
        return ApiResponse.<MedicalRecordResponse>builder()
                .success(true)
                .data(recordService.getById(recordId))
                .build();
    }

    @GetMapping("/citizen/{nationalId}")
    @Operation(summary = "Get all records for a citizen (doctors see only their clinic's records)")
    public ApiResponse<PageResponse<MedicalRecordResponse>> getByNationalId(
            @PathVariable String nationalId,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.<PageResponse<MedicalRecordResponse>>builder()
                .success(true)
                .data(recordService.getByNationalId(nationalId, page, size))
                .build();
    }

    @PatchMapping("/{recordId}/result")
    @Operation(summary = "Update the result of a record (e.g. PENDING → PASS). Republishes to SANLY Bridge.")
    public ApiResponse<MedicalRecordResponse> updateResult(
            @PathVariable UUID recordId,
            @Valid @RequestBody RecordResultUpdateRequest request) {
        MedicalRecordResponse body = recordService.updateResult(recordId, request);
        return ApiResponse.<MedicalRecordResponse>builder()
                .success(true)
                .message("Result updated and republished to SANLY Bridge")
                .data(body)
                .build();
    }
}
