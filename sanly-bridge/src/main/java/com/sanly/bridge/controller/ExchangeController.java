package com.sanly.bridge.controller;

import com.sanly.bridge.dto.request.ExchangePublishRequest;
import com.sanly.bridge.dto.request.ExchangeQueryRequest;
import com.sanly.bridge.dto.response.ApiResponse;
import com.sanly.bridge.dto.response.ExchangeResponse;
import com.sanly.bridge.dto.response.PublishedDataResponse;
import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.QueryPurpose;
import com.sanly.bridge.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All endpoints in this controller are authenticated via
 * {@code X-Institution-Code} + {@code X-Institution-Key} headers —
 * NOT via the admin JWT. The {@link com.sanly.bridge.config.InstitutionAuthFilter}
 * handles validation before this controller is reached.
 *
 * BREAKING CHANGE (Anti-Corruption Feature 1): Every query endpoint now requires
 * a {@code purposeCode}. Institutions must declare WHY they are requesting citizen data.
 * Sensitive data types (CRIMINAL_RECORD, MEDICAL_CLEARANCE) also require a {@code caseReference}.
 */
@Validated
@RestController
@RequestMapping("/api/v1/exchange")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTITUTION')")
@Tag(name = "Data Exchange",
     description = "Institution-to-bridge data exchange. Authenticate with " +
                   "X-Institution-Code and X-Institution-Key headers. " +
                   "purposeCode is required on all queries.")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping("/query")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Query data for a citizen — purposeCode required in request body")
    public ApiResponse<ExchangeResponse> query(
            @Valid @RequestBody ExchangeQueryRequest request,
            Authentication auth) {

        String institutionCode = (String) auth.getPrincipal();
        ExchangeResponse body = exchangeService.query(institutionCode, request);

        return ApiResponse.<ExchangeResponse>builder()
                .success(true)
                .data(body)
                .build();
    }

    @PostMapping("/publish")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publish a data record for a citizen into SANLY Bridge")
    public ApiResponse<ExchangeResponse> publish(
            @Valid @RequestBody ExchangePublishRequest request,
            Authentication auth) {

        String institutionCode = (String) auth.getPrincipal();
        ExchangeResponse body = exchangeService.publish(institutionCode, request);

        return ApiResponse.<ExchangeResponse>builder()
                .success(true)
                .message("Data published successfully")
                .data(body)
                .build();
    }

    @GetMapping("/data/{nationalId}/{dataType}")
    @Operation(summary = "Direct lookup — purposeCode required as query param; " +
                         "caseReference required for CRIMINAL_RECORD and MEDICAL_CLEARANCE")
    public ApiResponse<List<PublishedDataResponse>> getData(
            @Parameter(description = "Citizen's national ID")
            @PathVariable String nationalId,
            @Parameter(description = "Data type to query")
            @PathVariable DataType dataType,
            @Parameter(description = "Required — declared reason for this query")
            @RequestParam QueryPurpose purposeCode,
            @Parameter(description = "Case/court order reference — required for sensitive data types")
            @RequestParam(required = false) @Size(max = 100) String caseReference,
            @Parameter(description = "Optional free-text justification")
            @RequestParam(required = false) @Size(max = 500) String justification,
            Authentication auth) {

        String institutionCode = (String) auth.getPrincipal();
        List<PublishedDataResponse> records =
                exchangeService.getData(institutionCode, nationalId, dataType,
                        purposeCode, caseReference, justification);

        return ApiResponse.<List<PublishedDataResponse>>builder()
                .success(true)
                .data(records)
                .build();
    }
}
