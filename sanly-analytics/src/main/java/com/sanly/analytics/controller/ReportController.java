package com.sanly.analytics.controller;

import com.sanly.analytics.config.UserDetailsImpl;
import com.sanly.analytics.dto.request.GenerateReportRequest;
import com.sanly.analytics.dto.response.ApiResponse;
import com.sanly.analytics.dto.response.ReportStatusResponse;
import com.sanly.analytics.service.ReportGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportGenerationService reportService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReportStatusResponse>> generate(
            @Valid @RequestBody GenerateReportRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        UUID officerId = principal != null ? principal.getOfficer().getOfficerId() : null;
        return ResponseEntity.ok(ApiResponse.ok("Report generation started", reportService.initiateReport(req, officerId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportStatusResponse>>> list(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        UUID officerId = principal != null ? principal.getOfficer().getOfficerId() : null;
        return ResponseEntity.ok(ApiResponse.ok(reportService.listReports(officerId)));
    }

    @GetMapping("/{exportCode}/status")
    public ResponseEntity<ApiResponse<ReportStatusResponse>> status(@PathVariable String exportCode) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getStatus(exportCode)));
    }

    @GetMapping("/{exportCode}/download")
    public ResponseEntity<byte[]> download(@PathVariable String exportCode) {
        byte[] data = reportService.downloadPdf(exportCode);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", exportCode + ".pdf");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
