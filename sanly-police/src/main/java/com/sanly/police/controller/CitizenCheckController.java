package com.sanly.police.controller;

import com.sanly.police.dto.request.CitizenCheckRequest;
import com.sanly.police.dto.response.ApiResponse;
import com.sanly.police.dto.response.CitizenCheckResponse;
import com.sanly.police.service.CitizenCheckServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
@Tag(name = "Citizen Checks")
@SecurityRequirement(name = "bearerAuth")
public class CitizenCheckController {

    private final CitizenCheckServiceImpl checkService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CitizenCheckResponse> performCheck(@Valid @RequestBody CitizenCheckRequest req) {
        return ApiResponse.<CitizenCheckResponse>builder().success(true)
                .data(checkService.performCheck(req)).build();
    }

    @GetMapping("/{checkId}")
    public ApiResponse<CitizenCheckResponse> getById(@PathVariable UUID checkId) {
        return ApiResponse.<CitizenCheckResponse>builder().success(true)
                .data(checkService.getById(checkId)).build();
    }
}
