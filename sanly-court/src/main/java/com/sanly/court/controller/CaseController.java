package com.sanly.court.controller;

import com.sanly.court.dto.request.*;
import com.sanly.court.dto.response.CaseResponse;
import com.sanly.court.entity.CaseStatus;
import com.sanly.court.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/court/cases")
@RequiredArgsConstructor
public class CaseController {
    private final CaseService caseService;

    @PostMapping
    public ResponseEntity<CaseResponse> file(@RequestBody FileCaseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseService.file(req));
    }

    @GetMapping("/{caseNumber}")
    public ResponseEntity<CaseResponse> findByNumber(@PathVariable String caseNumber) {
        return ResponseEntity.ok(caseService.findByNumber(caseNumber));
    }

    // Public verify — permitted in SecurityConfig
    @GetMapping("/verify/{caseNumber}")
    public ResponseEntity<CaseResponse> verify(@PathVariable String caseNumber) {
        return ResponseEntity.ok(caseService.verify(caseNumber));
    }

    @GetMapping("/citizen/{nationalId}")
    public ResponseEntity<List<CaseResponse>> byCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(caseService.findByCitizen(nationalId));
    }

    @GetMapping("/court/{courtCode}")
    public ResponseEntity<List<CaseResponse>> byCourt(@PathVariable String courtCode,
                                                       @RequestParam(required = false) CaseStatus status) {
        return ResponseEntity.ok(caseService.findByCourt(courtCode, status));
    }

    @PatchMapping("/{caseNumber}/status")
    public ResponseEntity<CaseResponse> updateStatus(@PathVariable String caseNumber,
                                                      @RequestBody UpdateCaseStatusRequest req) {
        return ResponseEntity.ok(caseService.updateStatus(caseNumber, req));
    }

    @PatchMapping("/{caseNumber}/assign-judge")
    public ResponseEntity<CaseResponse> assignJudge(@PathVariable String caseNumber,
                                                     @RequestBody AssignJudgeRequest req) {
        return ResponseEntity.ok(caseService.assignJudge(caseNumber, req));
    }

    @PatchMapping("/{caseNumber}/schedule-hearing")
    public ResponseEntity<CaseResponse> scheduleHearing(@PathVariable String caseNumber,
                                                         @RequestBody ScheduleHearingRequest req) {
        return ResponseEntity.ok(caseService.scheduleHearing(caseNumber, req));
    }

    @PostMapping("/{caseNumber}/appeal")
    public ResponseEntity<CaseResponse> appeal(@PathVariable String caseNumber) {
        return ResponseEntity.ok(caseService.appeal(caseNumber));
    }
}
