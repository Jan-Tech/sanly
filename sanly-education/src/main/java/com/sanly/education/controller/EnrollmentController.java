package com.sanly.education.controller;

import com.sanly.education.dto.request.EnrollRequest;
import com.sanly.education.dto.response.EnrollmentResponse;
import com.sanly.education.entity.EnrollmentStatus;
import com.sanly.education.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/education/enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollRequest req,
                                                     Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(req, auth));
    }

    @GetMapping("/{enrollmentId}")
    public EnrollmentResponse getById(@PathVariable UUID enrollmentId) {
        return enrollmentService.findById(enrollmentId);
    }

    @GetMapping("/citizen/{nationalId}")
    public List<EnrollmentResponse> getByCitizen(@PathVariable String nationalId) {
        return enrollmentService.findByCitizen(nationalId);
    }

    @PatchMapping("/{enrollmentId}/status")
    public EnrollmentResponse updateStatus(@PathVariable UUID enrollmentId,
                                           @RequestBody Map<String, String> body,
                                           Authentication auth) {
        EnrollmentStatus status = EnrollmentStatus.valueOf(body.get("status"));
        return enrollmentService.updateStatus(enrollmentId, status, auth);
    }

    // Internal endpoint — called by sanly-civil life event automation
    @PostMapping("/pending-intake")
    public ResponseEntity<EnrollmentResponse> pendingIntake(@RequestBody Map<String, Object> body) {
        String childNationalId = (String) body.get("childNationalId");
        int expectedSchoolYear = (Integer) body.get("expectedSchoolYear");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.createPendingIntake(childNationalId, expectedSchoolYear));
    }

    @GetMapping("/pending")
    public List<EnrollmentResponse> getPendingIntakes() {
        return enrollmentService.findPendingIntakes();
    }
}
