package com.sanly.dmv.controller;

import com.sanly.dmv.dto.request.OfficerCreateRequest;
import com.sanly.dmv.dto.request.OfficerStatusRequest;
import com.sanly.dmv.dto.response.ApiResponse;
import com.sanly.dmv.dto.response.OfficerResponse;
import com.sanly.dmv.service.OfficerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Officers", description = "Officer account management — Admin only")
@SecurityRequirement(name = "bearerAuth")
public class OfficerController {

    private final OfficerServiceImpl officerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new DMV officer")
    public ApiResponse<OfficerResponse> register(@Valid @RequestBody OfficerCreateRequest request) {
        return ApiResponse.<OfficerResponse>builder()
                .success(true).message("Officer registered")
                .data(officerService.register(request)).build();
    }

    @GetMapping
    @Operation(summary = "List all officers")
    public ApiResponse<List<OfficerResponse>> listAll() {
        return ApiResponse.<List<OfficerResponse>>builder()
                .success(true).data(officerService.listAll()).build();
    }

    @PatchMapping("/{officerId}/status")
    @Operation(summary = "Activate or suspend an officer account")
    public ApiResponse<OfficerResponse> updateStatus(
            @PathVariable Long officerId,
            @Valid @RequestBody OfficerStatusRequest request) {
        return ApiResponse.<OfficerResponse>builder()
                .success(true)
                .message("Status updated to " + request.getStatus())
                .data(officerService.updateStatus(officerId, request))
                .build();
    }
}
