package com.sanly.analytics.controller;

import com.sanly.analytics.dto.request.*;
import com.sanly.analytics.dto.response.ApiResponse;
import com.sanly.analytics.service.IngestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/analytics/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final IngestService ingestService;

    @Value("${analytics.service-key:analytics-ingest-key-change-me}")
    private String expectedKey;

    @PostMapping("/daily")
    public ResponseEntity<ApiResponse<Void>> ingestDaily(
            @RequestHeader("X-Service-Key") String key,
            @Valid @RequestBody DailyIngestRequest req) {
        validateKey(key);
        ingestService.ingestDaily(req);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Snapshot updated").build());
    }

    @PostMapping("/service-usage")
    public ResponseEntity<ApiResponse<Void>> ingestUsage(
            @RequestHeader("X-Service-Key") String key,
            @Valid @RequestBody ServiceUsageIngestRequest req) {
        validateKey(key);
        ingestService.ingestServiceUsage(req);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Usage stat recorded").build());
    }

    @PostMapping("/regional")
    public ResponseEntity<ApiResponse<Void>> ingestRegional(
            @RequestHeader("X-Service-Key") String key,
            @Valid @RequestBody RegionalIngestRequest req) {
        validateKey(key);
        ingestService.ingestRegional(req);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Regional stat recorded").build());
    }

    @PostMapping("/anomaly")
    public ResponseEntity<ApiResponse<Void>> ingestAnomaly(
            @RequestHeader("X-Service-Key") String key,
            @Valid @RequestBody AnomalyIngestRequest req) {
        validateKey(key);
        ingestService.ingestAnomaly(req);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Anomaly trend recorded").build());
    }

    private void validateKey(String key) {
        if (!expectedKey.equals(key)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service key");
        }
    }
}
