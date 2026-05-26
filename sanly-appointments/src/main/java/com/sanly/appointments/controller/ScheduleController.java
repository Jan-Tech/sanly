package com.sanly.appointments.controller;

import com.sanly.appointments.dto.request.AddExceptionRequest;
import com.sanly.appointments.dto.request.SetScheduleRequest;
import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.entity.AvailabilityException;
import com.sanly.appointments.entity.AvailabilitySchedule;
import com.sanly.appointments.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Weekly availability schedules and exceptions")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    @Operation(summary = "Set or update a weekly schedule for an office")
    public ApiResponse<AvailabilitySchedule> setSchedule(@Valid @RequestBody SetScheduleRequest req) {
        return ApiResponse.<AvailabilitySchedule>builder().success(true)
                .data(scheduleService.setSchedule(req)).build();
    }

    @GetMapping("/office/{officeCode}")
    @Operation(summary = "Get the weekly schedule for an office — public")
    public ApiResponse<List<AvailabilitySchedule>> getSchedule(@PathVariable String officeCode) {
        return ApiResponse.<List<AvailabilitySchedule>>builder().success(true)
                .data(scheduleService.getSchedule(officeCode)).build();
    }

    @PostMapping("/exceptions")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    @Operation(summary = "Add a holiday or closure exception for an office")
    public ApiResponse<AvailabilityException> addException(@Valid @RequestBody AddExceptionRequest req) {
        return ApiResponse.<AvailabilityException>builder().success(true)
                .data(scheduleService.addException(req)).build();
    }

    @GetMapping("/exceptions/office/{officeCode}")
    @Operation(summary = "Get upcoming exceptions for an office — public")
    public ApiResponse<List<AvailabilityException>> getExceptions(@PathVariable String officeCode) {
        return ApiResponse.<List<AvailabilityException>>builder().success(true)
                .data(scheduleService.getExceptions(officeCode)).build();
    }
}
