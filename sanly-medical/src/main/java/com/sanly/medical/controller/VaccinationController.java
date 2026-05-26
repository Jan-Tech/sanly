package com.sanly.medical.controller;

import com.sanly.medical.config.UserDetailsImpl;
import com.sanly.medical.entity.VaccinationAppointment;
import com.sanly.medical.entity.VaccinationSchedule;
import com.sanly.medical.service.VaccinationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vaccinations")
@RequiredArgsConstructor
public class VaccinationController {

    private final VaccinationService vaccinationService;

    /**
     * Called by sanly-civil's LifeEventPublisherService on birth registration.
     * Authenticated by LifeEventKeyFilter (X-Life-Event-Key header → ROLE_LIFE_EVENT_SERVICE).
     */
    @PostMapping("/schedule")
    @PreAuthorize("hasRole('LIFE_EVENT_SERVICE')")
    public ResponseEntity<Map<String, Object>> createSchedule(@RequestBody CreateScheduleRequest req) {
        VaccinationSchedule schedule = vaccinationService.createSchedule(
                req.getCitizenNationalId(), req.getBirthDate(),
                req.getMotherNationalId(), req.getFatherNationalId());
        return ResponseEntity.ok(Map.of(
                "scheduleId",        schedule.getScheduleId().toString(),
                "citizenNationalId", schedule.getCitizenNationalId(),
                "milestonesCreated", 7
        ));
    }

    @GetMapping("/citizen/{nationalId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<VaccinationAppointment>> getForCitizen(@PathVariable String nationalId) {
        return ResponseEntity.ok(vaccinationService.getScheduleForCitizen(nationalId));
    }

    @PatchMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<VaccinationAppointment> markComplete(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(vaccinationService.markCompleted(appointmentId, principal.getDoctorId()));
    }

    @Data
    public static class CreateScheduleRequest {
        private String citizenNationalId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate birthDate;
        private String motherNationalId;
        private String fatherNationalId;
    }
}
