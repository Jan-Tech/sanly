package com.sanly.police.controller;

import com.sanly.police.dto.request.CriminalRecordCreateRequest;
import com.sanly.police.dto.request.RecordStatusRequest;
import com.sanly.police.dto.response.ApiResponse;
import com.sanly.police.dto.response.CriminalRecordResponse;
import com.sanly.police.service.CriminalRecordServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/criminal-records")
@RequiredArgsConstructor
@Tag(name = "Criminal Records")
@SecurityRequirement(name = "bearerAuth")
public class CriminalRecordController {

    private final CriminalRecordServiceImpl recordService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ApiResponse<CriminalRecordResponse> create(@Valid @RequestBody CriminalRecordCreateRequest req) {
        return ApiResponse.<CriminalRecordResponse>builder().success(true)
                .message("Criminal record submitted and queued for bridge publication")
                .data(recordService.create(req)).build();
    }

    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ApiResponse<CriminalRecordResponse> getById(@PathVariable UUID recordId) {
        return ApiResponse.<CriminalRecordResponse>builder().success(true)
                .data(recordService.getById(recordId)).build();
    }

    @GetMapping("/citizen/{nationalId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ApiResponse<List<CriminalRecordResponse>> getByCitizen(@PathVariable String nationalId) {
        return ApiResponse.<List<CriminalRecordResponse>>builder().success(true)
                .data(recordService.getByCitizen(nationalId)).build();
    }

    @PatchMapping("/{recordId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CriminalRecordResponse> updateStatus(@PathVariable UUID recordId,
                                                            @Valid @RequestBody RecordStatusRequest req) {
        return ApiResponse.<CriminalRecordResponse>builder().success(true)
                .data(recordService.updateStatus(recordId, req)).build();
    }
}
