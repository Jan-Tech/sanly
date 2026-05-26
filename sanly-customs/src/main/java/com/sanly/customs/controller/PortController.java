package com.sanly.customs.controller;

import com.sanly.customs.dto.request.CreatePortRequest;
import com.sanly.customs.dto.response.PortResponse;
import com.sanly.customs.entity.PortStatus;
import com.sanly.customs.service.PortService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customs/ports")
@RequiredArgsConstructor
public class PortController {
    private final PortService portService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PortResponse> create(@RequestBody CreatePortRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<PortResponse>> findAll() {
        return ResponseEntity.ok(portService.findAll());
    }

    @GetMapping("/{portCode}")
    public ResponseEntity<PortResponse> findByCode(@PathVariable String portCode) {
        return ResponseEntity.ok(portService.findByCode(portCode));
    }

    @PatchMapping("/{portCode}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PortResponse> updateStatus(@PathVariable String portCode,
                                                      @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(portService.updateStatus(portCode, PortStatus.valueOf(body.get("status"))));
    }
}
