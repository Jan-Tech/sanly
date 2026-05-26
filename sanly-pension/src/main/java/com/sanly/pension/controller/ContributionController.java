package com.sanly.pension.controller;

import com.sanly.pension.dto.request.SubmitContributionRequest;
import com.sanly.pension.dto.request.VerifyContributionRequest;
import com.sanly.pension.dto.response.ContributionResponse;
import com.sanly.pension.dto.response.EmployerResponse;
import com.sanly.pension.entity.Employer;
import com.sanly.pension.service.ContributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pension/contributions")
@RequiredArgsConstructor
public class ContributionController {
    private final ContributionService contributionService;

    @PostMapping
    public ResponseEntity<ContributionResponse> submit(@Valid @RequestBody SubmitContributionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contributionService.submit(req));
    }

    @GetMapping("/account/{accountCode}")
    public ResponseEntity<List<ContributionResponse>> findByAccount(@PathVariable String accountCode) {
        return ResponseEntity.ok(contributionService.findByAccount(accountCode));
    }

    @GetMapping("/employer/{employerCode}")
    public ResponseEntity<List<ContributionResponse>> findByEmployer(@PathVariable String employerCode) {
        return ResponseEntity.ok(contributionService.findByEmployer(employerCode));
    }

    @GetMapping("/due")
    public ResponseEntity<List<Employer>> findDue() {
        String month = LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue());
        return ResponseEntity.ok(contributionService.findEmployersWithMissingContributions(month));
    }

    @PatchMapping("/{contributionId}/verify")
    public ResponseEntity<ContributionResponse> verify(@PathVariable Long contributionId,
                                                        @Valid @RequestBody VerifyContributionRequest req) {
        return ResponseEntity.ok(contributionService.verify(contributionId, req));
    }
}
