package com.sanly.appointments.controller;

import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.dto.response.AppointmentResponse;
import com.sanly.appointments.dto.response.PageResponse;
import com.sanly.appointments.dto.response.StatsResponse;
import com.sanly.appointments.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Platform-wide appointment management")
public class AdminController {

    private final AppointmentService appointmentService;

    @GetMapping("/all")
    @Operation(summary = "All appointments across all offices, paginated")
    public ApiResponse<PageResponse<AppointmentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<PageResponse<AppointmentResponse>>builder().success(true)
                .data(appointmentService.getAllAppointments(page, size)).build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Platform-wide statistics")
    public ApiResponse<StatsResponse> getPlatformStats() {
        return ApiResponse.<StatsResponse>builder().success(true)
                .data(appointmentService.getPlatformStats()).build();
    }
}
