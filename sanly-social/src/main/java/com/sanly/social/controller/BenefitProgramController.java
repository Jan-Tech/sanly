package com.sanly.social.controller;

import com.sanly.social.dto.request.CreateProgramRequest;
import com.sanly.social.dto.response.ProgramResponse;
import com.sanly.social.entity.ProgramStatus;
import com.sanly.social.service.BenefitProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/social/programs")
@RequiredArgsConstructor
public class BenefitProgramController {

    private final BenefitProgramService programService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponse> create(@RequestBody CreateProgramRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponse>> findAll() {
        return ResponseEntity.ok(programService.findAll());
    }

    @GetMapping("/{programCode}")
    public ResponseEntity<ProgramResponse> findByCode(@PathVariable String programCode) {
        return ResponseEntity.ok(programService.findByCode(programCode));
    }

    @PatchMapping("/{programCode}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponse> updateStatus(@PathVariable String programCode,
                                                         @RequestBody Map<String, String> body) {
        ProgramStatus status = ProgramStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(programService.updateStatus(programCode, status));
    }
}
