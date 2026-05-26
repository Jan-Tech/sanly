package com.sanly.pension.controller;

import com.sanly.pension.dto.request.RegisterEmployerRequest;
import com.sanly.pension.dto.response.EmployerResponse;
import com.sanly.pension.entity.EmployerStatus;
import com.sanly.pension.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pension/employers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class EmployerController {
    private final EmployerService employerService;

    @PostMapping
    public ResponseEntity<EmployerResponse> register(@Valid @RequestBody RegisterEmployerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employerService.register(req));
    }

    @GetMapping
    public ResponseEntity<List<EmployerResponse>> findAll() {
        return ResponseEntity.ok(employerService.findAll());
    }

    @GetMapping("/{employerCode}")
    public ResponseEntity<EmployerResponse> findByCode(@PathVariable String employerCode) {
        return ResponseEntity.ok(employerService.findByCode(employerCode));
    }

    @PatchMapping("/{employerCode}/status")
    public ResponseEntity<EmployerResponse> updateStatus(@PathVariable String employerCode,
                                                          @RequestParam EmployerStatus status) {
        return ResponseEntity.ok(employerService.updateStatus(employerCode, status));
    }
}
