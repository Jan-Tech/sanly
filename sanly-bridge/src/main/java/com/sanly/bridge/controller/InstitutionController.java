package com.sanly.bridge.controller;

import com.sanly.bridge.dto.request.InstitutionCreateRequest;
import com.sanly.bridge.dto.request.InstitutionStatusRequest;
import com.sanly.bridge.dto.response.ApiResponse;
import com.sanly.bridge.dto.response.InstitutionKeyResponse;
import com.sanly.bridge.dto.response.InstitutionResponse;
import com.sanly.bridge.service.InstitutionService;
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
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Institutions", description = "Register and manage government institutions")
@SecurityRequirement(name = "bearerAuth")
public class InstitutionController {

    private final InstitutionService institutionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new institution (returns one-time API key)")
    public ApiResponse<InstitutionKeyResponse> register(
            @Valid @RequestBody InstitutionCreateRequest request) {
        InstitutionKeyResponse body = institutionService.register(request);
        return ApiResponse.<InstitutionKeyResponse>builder()
                .success(true)
                .message("Institution registered. Store the API key securely — it will not be shown again.")
                .data(body)
                .build();
    }

    @GetMapping
    @Operation(summary = "List all registered institutions")
    public ApiResponse<List<InstitutionResponse>> listAll() {
        return ApiResponse.<List<InstitutionResponse>>builder()
                .success(true)
                .data(institutionService.listAll())
                .build();
    }

    @GetMapping("/{institutionCode}")
    @Operation(summary = "Get institution details by code")
    public ApiResponse<InstitutionResponse> getByCode(
            @PathVariable String institutionCode) {
        return ApiResponse.<InstitutionResponse>builder()
                .success(true)
                .data(institutionService.getByCode(institutionCode))
                .build();
    }

    @PatchMapping("/{institutionCode}/status")
    @Operation(summary = "Activate or suspend an institution")
    public ApiResponse<InstitutionResponse> updateStatus(
            @PathVariable String institutionCode,
            @Valid @RequestBody InstitutionStatusRequest request) {
        return ApiResponse.<InstitutionResponse>builder()
                .success(true)
                .message("Status updated to " + request.getStatus())
                .data(institutionService.updateStatus(institutionCode, request))
                .build();
    }

    @PostMapping("/{institutionCode}/rotate-key")
    @Operation(summary = "Rotate the institution's API key (old key is immediately invalidated)")
    public ApiResponse<InstitutionKeyResponse> rotateKey(
            @PathVariable String institutionCode) {
        InstitutionKeyResponse body = institutionService.rotateKey(institutionCode);
        return ApiResponse.<InstitutionKeyResponse>builder()
                .success(true)
                .message("API key rotated. Store the new key securely — it will not be shown again.")
                .data(body)
                .build();
    }
}
