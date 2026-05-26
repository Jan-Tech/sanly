package com.sanly.business.controller;

import com.sanly.business.client.NotificationClient;
import com.sanly.business.entity.Business;
import com.sanly.business.entity.BusinessStatus;
import com.sanly.business.repository.BusinessRepository;
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
 * Internal endpoint called by sanly-civil on death registration.
 * Suspends all ACTIVE businesses owned by the deceased and notifies co-owners if present.
 */
@Slf4j
@RestController
@PreAuthorize("hasRole('LIFE_EVENT_SERVICE')")
@RequiredArgsConstructor
public class InternalBusinessController {

    private final BusinessRepository businessRepository;
    private final NotificationClient notificationClient;

    @PostMapping("/api/v1/businesses/owner-deceased")
    @Transactional
    public ResponseEntity<Map<String, Object>> handleOwnerDeceased(
            @RequestBody Map<String, String> req) {

        String deceasedNin = req.get("nationalId");
        String deceasedName = req.getOrDefault("deceasedFullName", "the deceased owner");
        String dateOfDeath  = req.getOrDefault("dateOfDeath", "");

        if (deceasedNin == null || deceasedNin.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "nationalId is required"));
        }

        List<Business> activeBusinesses = businessRepository
                .findByOwnerNationalId(deceasedNin, PageRequest.of(0, 100))
                .stream()
                .filter(b -> b.getStatus() == BusinessStatus.ACTIVE)
                .toList();

        for (Business business : activeBusinesses) {
            business.setStatus(BusinessStatus.SUSPENDED);
            businessRepository.save(business);

            notificationClient.send(deceasedNin, "BUSINESS_OWNER_DECEASED", "EN",
                    Map.of(
                            "businessName",        business.getBusinessName(),
                            "registrationNumber",  business.getRegistrationNumber(),
                            "deceasedFullName",    deceasedName,
                            "dateOfDeath",         dateOfDeath
                    ));
        }

        log.info("Suspended {} businesses for deceased NIN={}", activeBusinesses.size(), deceasedNin);
        return ResponseEntity.ok(Map.of(
                "nationalId",      deceasedNin,
                "suspendedCount",  activeBusinesses.size()
        ));
    }
}
