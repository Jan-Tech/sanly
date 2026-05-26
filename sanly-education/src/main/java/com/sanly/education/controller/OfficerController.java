package com.sanly.education.controller;

import com.sanly.education.dto.request.CreateOfficerRequest;
import com.sanly.education.dto.response.OfficerResponse;
import com.sanly.education.entity.OfficerStatus;
import com.sanly.education.service.OfficerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/education/officers")
public class OfficerController {
    private final OfficerService officerService;

    public OfficerController(OfficerService officerService) {
        this.officerService = officerService;
    }

    @PostMapping
    public ResponseEntity<OfficerResponse> create(@Valid @RequestBody CreateOfficerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officerService.create(req));
    }

    @GetMapping
    public List<OfficerResponse> listAll() {
        return officerService.findAll();
    }

    @PatchMapping("/{officerId}/status")
    public OfficerResponse updateStatus(@PathVariable UUID officerId,
                                        @RequestBody Map<String, String> body) {
        OfficerStatus status = OfficerStatus.valueOf(body.get("status"));
        return officerService.updateStatus(officerId, status);
    }
}
