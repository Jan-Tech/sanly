package com.sanly.tax.controller;

import com.sanly.tax.dto.request.TaxpayerRegisterRequest;
import com.sanly.tax.dto.response.ApiResponse;
import com.sanly.tax.dto.response.ComplianceResponse;
import com.sanly.tax.dto.response.TaxpayerResponse;
import com.sanly.tax.service.TaxpayerServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/taxpayers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
@Tag(name = "Taxpayers")
@SecurityRequirement(name = "bearerAuth")
public class TaxpayerController {

    private final TaxpayerServiceImpl taxpayerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaxpayerResponse> register(@Valid @RequestBody TaxpayerRegisterRequest req) {
        return ApiResponse.<TaxpayerResponse>builder().success(true)
                .message("Taxpayer registered. Tax ID: " + req.getCitizenNationalId())
                .data(taxpayerService.register(req)).build();
    }

    @GetMapping("/{nationalId}")
    public ApiResponse<TaxpayerResponse> getByNationalId(@PathVariable String nationalId) {
        return ApiResponse.<TaxpayerResponse>builder().success(true)
                .data(taxpayerService.getByNationalId(nationalId)).build();
    }

    @GetMapping("/{nationalId}/compliance")
    public ApiResponse<ComplianceResponse> getCompliance(@PathVariable String nationalId) {
        return ApiResponse.<ComplianceResponse>builder().success(true)
                .data(taxpayerService.getCompliance(nationalId)).build();
    }
}
