package com.sanly.appointments.controller;

import com.sanly.appointments.dto.request.WaitlistRequest;
import com.sanly.appointments.dto.response.ApiResponse;
import com.sanly.appointments.entity.WaitlistEntry;
import com.sanly.appointments.service.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments/waitlist")
@RequiredArgsConstructor
@Tag(name = "Waitlist", description = "Join or leave the waitlist for an office/service")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Join the waitlist for an office/service when no slots are available")
    public ApiResponse<WaitlistEntry> join(@Valid @RequestBody WaitlistRequest req) {
        String nationalId = extractNationalId();
        return ApiResponse.<WaitlistEntry>builder().success(true)
                .message("Added to waitlist. You will be notified when a slot becomes available.")
                .data(waitlistService.join(req, nationalId)).build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Get the citizen's waitlist entries")
    public ApiResponse<List<WaitlistEntry>> getMy() {
        return ApiResponse.<List<WaitlistEntry>>builder().success(true)
                .data(waitlistService.getCitizenWaitlist(extractNationalId())).build();
    }

    @DeleteMapping("/{waitlistId}")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Leave the waitlist")
    public ApiResponse<Void> leave(@PathVariable UUID waitlistId) {
        waitlistService.leave(waitlistId, extractNationalId());
        return ApiResponse.<Void>builder().success(true).message("Removed from waitlist.").build();
    }

    private String extractNationalId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String s) return s;
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Not a citizen token.");
    }
}
