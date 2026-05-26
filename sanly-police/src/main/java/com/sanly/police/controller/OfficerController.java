package com.sanly.police.controller;

import com.sanly.police.dto.request.OfficerCreateRequest;
import com.sanly.police.dto.request.OfficerStatusRequest;
import com.sanly.police.dto.response.ApiResponse;
import com.sanly.police.dto.response.OfficerResponse;
import com.sanly.police.service.OfficerServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Officers")
@SecurityRequirement(name = "bearerAuth")
public class OfficerController {

    private final OfficerServiceImpl officerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OfficerResponse> create(@Valid @RequestBody OfficerCreateRequest req) {
        return ApiResponse.<OfficerResponse>builder().success(true)
                .data(officerService.create(req)).build();
    }

    @GetMapping
    public ApiResponse<List<OfficerResponse>> listAll() {
        return ApiResponse.<List<OfficerResponse>>builder().success(true)
                .data(officerService.listAll()).build();
    }

    @PatchMapping("/{officerId}/status")
    public ApiResponse<OfficerResponse> updateStatus(@PathVariable Long officerId,
                                                     @Valid @RequestBody OfficerStatusRequest req) {
        return ApiResponse.<OfficerResponse>builder().success(true)
                .data(officerService.updateStatus(officerId, req)).build();
    }
}
