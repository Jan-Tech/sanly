package com.sanly.appointments.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class WaitlistRequest {

    @NotBlank
    private String officeCode;

    private UUID serviceTypeId;

    private LocalDate preferredDateFrom;
    private LocalDate preferredDateTo;
}
