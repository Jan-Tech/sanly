package com.sanly.tax.controller;

import com.sanly.tax.dto.request.OfficerCreateRequest;
import com.sanly.tax.dto.request.OfficerStatusRequest;
import com.sanly.tax.dto.response.ApiResponse;
import com.sanly.tax.dto.response.OfficerResponse;
import com.sanly.tax.service.OfficerServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/officers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OfficerController {
    private final OfficerServiceImpl officerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OfficerResponse> create(@Valid @RequestBody OfficerCreateRequest req) {
        return ApiResponse.<OfficerResponse>builder().success(true).data(officerService.create(req)).build();
    }

    @GetMapping
    public ApiResponse<List<OfficerResponse>> listAll() {
        return ApiResponse.<List<OfficerResponse>>builder().success(true).data(officerService.listAll()).build();
    }

    @PatchMapping("/{officerId}/status")
    public ApiResponse<OfficerResponse> updateStatus(@PathVariable Long officerId,
                                                     @Valid @RequestBody OfficerStatusRequest req) {
        return ApiResponse.<OfficerResponse>builder().success(true)
                .data(officerService.updateStatus(officerId, req)).build();
    }
}
