package com.sanly.dmv.controller;

import com.sanly.dmv.entity.DrivingLicense;
import com.sanly.dmv.entity.LicenseStatus;
import com.sanly.dmv.repository.DrivingLicenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Internal endpoint called by sanly-civil's LifeEventPublisherService on death registration.
 * Revokes all ACTIVE licenses for the deceased citizen.
 */
@Slf4j
@RestController
@PreAuthorize("hasRole('LIFE_EVENT_SERVICE')")
@RequiredArgsConstructor
public class InternalLicenseController {

    private final DrivingLicenseRepository licenseRepository;

    @PostMapping("/api/v1/licenses/suspend-deceased")
    @Transactional
    public ResponseEntity<Map<String, Object>> suspendDeceasedLicenses(
            @RequestBody Map<String, String> req) {

        String nationalId = req.get("nationalId");
        if (nationalId == null || nationalId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "nationalId is required"));
        }

        List<DrivingLicense> activeLicenses = licenseRepository
                .findByCitizenNationalIdOrderByIssuedAtDesc(nationalId,
                        PageRequest.of(0, 100))
                .stream()
                .filter(l -> l.getStatus() == LicenseStatus.ACTIVE)
                .toList();

        for (DrivingLicense license : activeLicenses) {
            license.setStatus(LicenseStatus.REVOKED);
            licenseRepository.save(license);
        }

        log.info("Revoked {} licenses for deceased NIN={}", activeLicenses.size(), nationalId);
        return ResponseEntity.ok(Map.of(
                "nationalId", nationalId,
                "revokedCount", activeLicenses.size()
        ));
    }
}
