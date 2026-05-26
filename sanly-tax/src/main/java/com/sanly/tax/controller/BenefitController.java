package com.sanly.tax.controller;

import com.sanly.tax.entity.ChildBenefitRecord;
import com.sanly.tax.entity.MaritalStatus;
import com.sanly.tax.service.BenefitService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Internal endpoints called by sanly-civil's LifeEventPublisherService.
 * Authentication via X-Life-Event-Key header (LifeEventKeyFilter).
 */
@RestController
@PreAuthorize("hasRole('LIFE_EVENT_SERVICE')")
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;

    @PostMapping("/api/v1/benefits/child-benefit")
    public ResponseEntity<Map<String, Object>> createChildBenefit(@RequestBody ChildBenefitRequest req) {
        ChildBenefitRecord record = benefitService.createChildBenefit(
                req.getChildNationalId(), req.getMotherNationalId(),
                req.getFatherNationalId(), req.getBirthDate());
        return ResponseEntity.ok(Map.of(
                "benefitId",        record != null ? record.getBenefitId().toString() : "existing",
                "childNationalId",  req.getChildNationalId(),
                "status",           record != null ? record.getStatus().name() : "ACTIVE"
        ));
    }

    @PostMapping("/api/v1/benefits/cancel")
    public ResponseEntity<Map<String, Object>> cancelBenefits(@RequestBody CancelBenefitsRequest req) {
        int cancelled = benefitService.cancelAllBenefits(req.getNationalId());
        return ResponseEntity.ok(Map.of("cancelled", cancelled, "nationalId", req.getNationalId()));
    }

    @PostMapping("/api/v1/taxpayers/update-marital-status")
    public ResponseEntity<Map<String, String>> updateMaritalStatus(
            @RequestBody UpdateMaritalStatusRequest req) {
        benefitService.updateMaritalStatus(req.getNationalId(),
                MaritalStatus.valueOf(req.getMaritalStatus()), req.getSpouseNationalId());
        return ResponseEntity.ok(Map.of("nationalId", req.getNationalId(), "status", req.getMaritalStatus()));
    }

    @PostMapping("/api/v1/taxpayers/deregister")
    public ResponseEntity<Map<String, String>> deregisterTaxpayer(
            @RequestBody Map<String, String> req) {
        benefitService.deregisterTaxpayer(req.get("nationalId"));
        return ResponseEntity.ok(Map.of("nationalId", req.get("nationalId"), "status", "DEREGISTERED"));
    }

    // ── Inline request DTOs — internal endpoints only ────────────────────────

    @Data
    public static class ChildBenefitRequest {
        private String childNationalId;
        private String motherNationalId;
        private String fatherNationalId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate birthDate;
    }

    @Data
    public static class CancelBenefitsRequest {
        private String nationalId;
    }

    @Data
    public static class UpdateMaritalStatusRequest {
        private String nationalId;
        private String maritalStatus;
        private String spouseNationalId;
    }
}
