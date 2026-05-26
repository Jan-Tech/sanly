package com.sanly.social.controller;

import com.sanly.social.dto.request.RegisterUnemploymentRequest;
import com.sanly.social.dto.response.UnemploymentResponse;
import com.sanly.social.service.UnemploymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/social/unemployment")
@RequiredArgsConstructor
public class UnemploymentController {

    private final UnemploymentService unemploymentService;

    @PostMapping
    public ResponseEntity<UnemploymentResponse> register(@RequestBody RegisterUnemploymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(unemploymentService.register(req));
    }

    @GetMapping("/{nationalId}")
    public ResponseEntity<UnemploymentResponse> findByNationalId(@PathVariable String nationalId) {
        return ResponseEntity.ok(unemploymentService.findByNationalId(nationalId));
    }

    @PatchMapping("/{nationalId}/mark-employed")
    public ResponseEntity<UnemploymentResponse> markEmployed(@PathVariable String nationalId) {
        return ResponseEntity.ok(unemploymentService.markEmployed(nationalId));
    }
}
