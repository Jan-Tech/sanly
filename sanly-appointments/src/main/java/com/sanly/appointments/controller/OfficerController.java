package com.sanly.appointments.controller;

import com.sanly.appointments.config.UserDetailsImpl;
import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.dto.response.AppointmentResponse;
import com.sanly.appointments.dto.response.StatsResponse;
import com.sanly.appointments.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Officer", description = "Officer view — daily schedule and appointment management")
public class OfficerController {

    private final AppointmentService appointmentService;

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    @Operation(summary = "Today's appointments for the officer's office, sorted by slot time")
    public ApiResponse<List<AppointmentResponse>> getToday(
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ApiResponse.<List<AppointmentResponse>>builder().success(true)
                .data(appointmentService.getOfficeSchedule(principal.getOfficeCode(), LocalDate.now())).build();
    }

    @GetMapping("/schedule/{officeCode}")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    @Operation(summary = "Full day schedule for a specific office and date")
    public ApiResponse<List<AppointmentResponse>> getSchedule(
            @PathVariable String officeCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.<List<AppointmentResponse>>builder().success(true)
                .data(appointmentService.getOfficeSchedule(officeCode, date)).build();
    }

    @PatchMapping("/{appointmentCode}/complete")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    @Operation(summary = "Mark an appointment as completed")
    public ApiResponse<AppointmentResponse> complete(
            @PathVariable String appointmentCode,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ApiResponse.<AppointmentResponse>builder().success(true)
                .data(appointmentService.complete(appointmentCode, principal.getId())).build();
    }

    @PatchMapping("/{appointmentCode}/no-show")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    @Operation(summary = "Mark a citizen as no-show")
    public ApiResponse<AppointmentResponse> markNoShow(
            @PathVariable String appointmentCode,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return ApiResponse.<AppointmentResponse>builder().success(true)
                .data(appointmentService.markNoShow(appointmentCode, principal.getId())).build();
    }

    @GetMapping("/stats/{officeCode}")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    @Operation(summary = "Statistics for an office — completion rate, no-show rate, average rating, busiest slot")
    public ApiResponse<StatsResponse> getStats(@PathVariable String officeCode) {
        return ApiResponse.<StatsResponse>builder().success(true)
                .data(appointmentService.getStats(officeCode)).build();
    }
}
