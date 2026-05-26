package com.sanly.appointments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class BookAppointmentRequest {

    @NotBlank
    private String officeCode;

    @NotNull
    private UUID serviceTypeId;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime slotTime;

    private String notes;
}
