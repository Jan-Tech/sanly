package com.sanly.analytics.controller;

import com.sanly.analytics.dto.response.*;
import com.sanly.analytics.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics/kpi")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    @GetMapping("/population")
    public ResponseEntity<ApiResponse<PopulationKpiResponse>> getPopulation() {
        return ResponseEntity.ok(ApiResponse.ok(kpiService.getPopulationKpi()));
    }

    @GetMapping("/economy")
    public ResponseEntity<ApiResponse<EconomyKpiResponse>> getEconomy() {
        return ResponseEntity.ok(ApiResponse.ok(kpiService.getEconomyKpi()));
    }

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<ServicesKpiResponse>> getServices() {
        return ResponseEntity.ok(ApiResponse.ok(kpiService.getServicesKpi()));
    }

    @GetMapping("/anti-corruption")
    public ResponseEntity<ApiResponse<AntiCorruptionKpiResponse>> getAntiCorruption() {
        return ResponseEntity.ok(ApiResponse.ok(kpiService.getAntiCorruptionKpi()));
    }
}
