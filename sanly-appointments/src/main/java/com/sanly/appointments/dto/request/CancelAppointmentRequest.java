package com.sanly.appointments.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelAppointmentRequest {

    @NotBlank
    private String reason;
}
