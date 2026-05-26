package com.sanly.court.controller;

import com.sanly.court.dto.request.IssueVerdictRequest;
import com.sanly.court.dto.response.VerdictResponse;
import com.sanly.court.service.VerdictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/court/cases/{caseNumber}/verdict")
@RequiredArgsConstructor
public class VerdictController {
    private final VerdictService verdictService;

    @PostMapping
    @PreAuthorize("hasRole('JUDGE')")
    public ResponseEntity<VerdictResponse> issue(@PathVariable String caseNumber,
                                                  @RequestBody IssueVerdictRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verdictService.issue(caseNumber, req));
    }

    @GetMapping
    public ResponseEntity<VerdictResponse> findByCaseNumber(@PathVariable String caseNumber) {
        return ResponseEntity.ok(verdictService.findByCaseNumber(caseNumber));
    }
}
