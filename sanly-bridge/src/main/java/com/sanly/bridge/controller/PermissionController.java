package com.sanly.bridge.controller;

import com.sanly.bridge.dto.request.PermissionCreateRequest;
import com.sanly.bridge.dto.response.ApiResponse;
import com.sanly.bridge.dto.response.PermissionResponse;
import com.sanly.bridge.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Permissions", description = "Grant and revoke inter-institution data access permissions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Grant institution A permission to query data type D from institution B")
    public ApiResponse<PermissionResponse> grant(
            @Valid @RequestBody PermissionCreateRequest request,
            Authentication authentication) {
        String grantedBy = authentication != null ? authentication.getName() : "system";
        return ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permission granted: " + request.getRequestingCode()
                         + " → " + request.getTargetCode()
                         + " [" + request.getDataType() + "]")
                .data(permissionService.grant(request, grantedBy))
                .build();
    }

    @GetMapping
    @Operation(summary = "List all active permissions")
    public ApiResponse<List<PermissionResponse>> listAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .data(permissionService.listAll())
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke a permission (soft-delete — record kept for audit)")
    public void revoke(@PathVariable Long id) {
        permissionService.revoke(id);
    }
}
