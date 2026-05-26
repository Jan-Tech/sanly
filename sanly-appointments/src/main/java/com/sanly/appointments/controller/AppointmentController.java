package com.sanly.appointments.controller;

import com.sanly.appointments.dto.request.BookAppointmentRequest;
import com.sanly.appointments.dto.request.CancelAppointmentRequest;
import com.sanly.appointments.dto.request.RateAppointmentRequest;
import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.dto.response.AppointmentResponse;
import com.sanly.appointments.dto.response.PageResponse;
import com.sanly.appointments.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Citizen booking and management endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    @PreAuthorize("hasRole('CITIZEN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book an appointment at a government office")
    public ApiResponse<AppointmentResponse> book(@Valid @RequestBody BookAppointmentRequest req,
                                                  HttpServletRequest httpRequest) {
        String nationalId = extractNationalId();
        String ip = resolveIp(httpRequest);
        return ApiResponse.<AppointmentResponse>builder().success(true)
                .message("Appointment booked successfully.")
                .data(appointmentService.book(req, nationalId, ip)).build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "List all appointments for the authenticated citizen")
    public ApiResponse<PageResponse<AppointmentResponse>> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<AppointmentResponse>>builder().success(true)
                .data(appointmentService.getMyAppointments(extractNationalId(), page, size)).build();
    }

    @GetMapping("/my/{appointmentCode}")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Get details of a specific appointment")
    public ApiResponse<AppointmentResponse> getMyAppointment(@PathVariable String appointmentCode) {
        return ApiResponse.<AppointmentResponse>builder().success(true)
                .data(appointmentService.getMyAppointment(extractNationalId(), appointmentCode)).build();
    }

    @PatchMapping("/my/{appointmentCode}/cancel")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Cancel an appointment (must be at least 2 hours before the slot)")
    public ApiResponse<AppointmentResponse> cancelAppointment(
            @PathVariable String appointmentCode,
            @Valid @RequestBody CancelAppointmentRequest req) {
        return ApiResponse.<AppointmentResponse>builder().success(true)
                .message("Appointment cancelled.")
                .data(appointmentService.cancel(appointmentCode, extractNationalId(), req.getReason())).build();
    }

    @PostMapping("/my/{appointmentCode}/rate")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Rate a completed appointment (1–5 stars + optional feedback)")
    public ApiResponse<AppointmentResponse> rateAppointment(
            @PathVariable String appointmentCode,
            @Valid @RequestBody RateAppointmentRequest req) {
        return ApiResponse.<AppointmentResponse>builder().success(true)
                .message("Thank you for your feedback!")
                .data(appointmentService.rate(appointmentCode, extractNationalId(), req.getRating(), req.getFeedback())).build();
    }

    private String extractNationalId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String s) return s;
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Not a citizen token.");
    }

    private static String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
