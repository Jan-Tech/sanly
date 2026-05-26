package com.sanly.tax.controller;

import com.sanly.tax.dto.request.FilingProcessRequest;
import com.sanly.tax.dto.request.TaxFilingCreateRequest;
import com.sanly.tax.dto.response.ApiResponse;
import com.sanly.tax.dto.response.TaxFilingResponse;
import com.sanly.tax.service.TaxFilingServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/filings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
@Tag(name = "Tax Filings")
@SecurityRequirement(name = "bearerAuth")
public class TaxFilingController {

    private final TaxFilingServiceImpl filingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaxFilingResponse> create(@Valid @RequestBody TaxFilingCreateRequest req) {
        return ApiResponse.<TaxFilingResponse>builder().success(true)
                .data(filingService.create(req)).build();
    }

    @GetMapping("/{filingId}")
    public ApiResponse<TaxFilingResponse> getById(@PathVariable UUID filingId) {
        return ApiResponse.<TaxFilingResponse>builder().success(true)
                .data(filingService.getById(filingId)).build();
    }

    @GetMapping("/taxpayer/{taxId}")
    public ApiResponse<List<TaxFilingResponse>> getByTaxId(@PathVariable String taxId) {
        return ApiResponse.<List<TaxFilingResponse>>builder().success(true)
                .data(filingService.getByTaxId(taxId)).build();
    }

    @PatchMapping("/{filingId}/process")
    public ApiResponse<TaxFilingResponse> process(@PathVariable UUID filingId,
                                                  @Valid @RequestBody FilingProcessRequest req) {
        return ApiResponse.<TaxFilingResponse>builder().success(true)
                .data(filingService.process(filingId, req)).build();
    }
}
