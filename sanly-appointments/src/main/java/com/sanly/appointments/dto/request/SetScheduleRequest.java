package com.sanly.appointments.dto.request;

import com.sanly.appointments.entity.ScheduleDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalTime;

@Data
public class SetScheduleRequest {

    @NotBlank
    private String officeCode;

    @NotNull
    private ScheduleDay dayOfWeek;

    @NotNull
    private LocalTime openTime;

    @NotNull
    private LocalTime closeTime;

    @Positive
    private int slotDurationMinutes = 30;

    @Positive
    private int maxConcurrentAppointments = 3;

    private boolean isActive = true;
}
