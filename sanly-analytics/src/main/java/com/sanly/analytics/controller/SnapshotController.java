package com.sanly.analytics.controller;

import com.sanly.analytics.dto.response.ApiResponse;
import com.sanly.analytics.dto.response.DashboardResponse;
import com.sanly.analytics.dto.response.TrendPoint;
import com.sanly.analytics.entity.AnomalyTrend;
import com.sanly.analytics.entity.DailySnapshot;
import com.sanly.analytics.entity.RegionalStat;
import com.sanly.analytics.entity.ServiceUsageStat;
import com.sanly.analytics.repository.AnomalyTrendRepository;
import com.sanly.analytics.repository.RegionalStatRepository;
import com.sanly.analytics.repository.ServiceUsageStatRepository;
import com.sanly.analytics.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SnapshotController {

    private final SnapshotService snapshotService;
    private final RegionalStatRepository regionalRepo;
    private final ServiceUsageStatRepository usageRepo;
    private final AnomalyTrendRepository anomalyRepo;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(snapshotService.getDashboard()));
    }

    @GetMapping("/snapshots")
    public ResponseEntity<ApiResponse<List<DailySnapshot>>> getHistorical(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(snapshotService.getHistorical(from, to)));
    }

    @GetMapping("/snapshots/{date}")
    public ResponseEntity<ApiResponse<DailySnapshot>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(snapshotService.getByDate(date)));
    }

    @GetMapping("/trends/{metric}")
    public ResponseEntity<ApiResponse<List<TrendPoint>>> getTrend(
            @PathVariable String metric,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.ok(snapshotService.getTrend(metric, days)));
    }

    @GetMapping("/regional")
    public ResponseEntity<ApiResponse<List<RegionalStat>>> getRegional() {
        return ResponseEntity.ok(ApiResponse.ok(regionalRepo.findLatestAll()));
    }

    @GetMapping("/service-usage")
    public ResponseEntity<ApiResponse<List<ServiceUsageStat>>> getServiceUsage(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(7);
        if (to == null) to = LocalDate.now();
        List<ServiceUsageStat> result = service != null
                ? usageRepo.findByServiceNameAndStatDateBetween(service, from, to)
                : usageRepo.findByStatDateBetweenOrderByStatDateDesc(from, to);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/anomaly-trends")
    public ResponseEntity<ApiResponse<List<AnomalyTrend>>> getAnomalyTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(anomalyRepo.findByStatDateBetweenOrderByStatDateDesc(from, to)));
    }
}
