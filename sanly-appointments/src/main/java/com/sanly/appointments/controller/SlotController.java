package com.sanly.appointments.controller;

import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.dto.response.SlotResponse;
import com.sanly.appointments.service.SlotCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments/slots")
@RequiredArgsConstructor
@Tag(name = "Slots", description = "Slot availability — public endpoints")
public class SlotController {

    private final SlotCalculationService slotCalcService;

    @GetMapping("/{officeCode}")
    @Operation(summary = "Get available time slots for a specific office, date, and service — public")
    public ApiResponse<List<SlotResponse>> getSlots(
            @PathVariable String officeCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID serviceTypeId) {
        return ApiResponse.<List<SlotResponse>>builder().success(true)
                .data(slotCalcService.getAvailableSlots(officeCode, date, serviceTypeId)).build();
    }

    @GetMapping("/{officeCode}/next-available")
    @Operation(summary = "Get the next 5 dates with available slots — public")
    public ApiResponse<List<LocalDate>> getNextAvailableDates(
            @PathVariable String officeCode,
            @RequestParam(required = false) UUID serviceTypeId) {
        return ApiResponse.<List<LocalDate>>builder().success(true)
                .data(slotCalcService.getNextAvailableDates(officeCode, serviceTypeId, LocalDate.now().plusDays(1))).build();
    }
}
