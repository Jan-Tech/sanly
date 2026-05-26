package com.sanly.education.controller;

import com.sanly.education.dto.request.IssueDiplomaRequest;
import com.sanly.education.dto.response.DiplomaResponse;
import com.sanly.education.service.DiplomaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/education/diplomas")
public class DiplomaController {
    private final DiplomaService diplomaService;

    public DiplomaController(DiplomaService diplomaService) {
        this.diplomaService = diplomaService;
    }

    @PostMapping
    public ResponseEntity<DiplomaResponse> issue(@Valid @RequestBody IssueDiplomaRequest req,
                                                 Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diplomaService.issue(req, auth));
    }

    @GetMapping("/{diplomaCode}")
    public DiplomaResponse getByCode(@PathVariable String diplomaCode) {
        return diplomaService.findByCode(diplomaCode);
    }

    @GetMapping("/citizen/{nationalId}")
    public List<DiplomaResponse> getByCitizen(@PathVariable String nationalId) {
        return diplomaService.findByCitizen(nationalId);
    }

    // Public endpoint — no auth required; used by employers to verify diplomas
    @GetMapping("/verify/{diplomaCode}")
    public DiplomaResponse verify(@PathVariable String diplomaCode) {
        return diplomaService.verifyPublic(diplomaCode);
    }

    @PatchMapping("/{diplomaCode}/revoke")
    public DiplomaResponse revoke(@PathVariable String diplomaCode,
                                  @RequestBody Map<String, String> body) {
        return diplomaService.revoke(diplomaCode, body.get("reason"));
    }

    @GetMapping
    public List<DiplomaResponse> listAll() {
        return diplomaService.findAll();
    }
}
