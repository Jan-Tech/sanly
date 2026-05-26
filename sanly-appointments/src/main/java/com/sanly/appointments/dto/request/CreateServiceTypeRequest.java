package com.sanly.appointments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateServiceTypeRequest {

    @NotBlank
    @Size(max = 12)
    private String officeCode;

    @NotBlank
    @Size(max = 200)
    private String serviceName;

    @Size(max = 1000)
    private String description;

    @Positive
    private int durationMinutes = 30;

    @Size(max = 1000)
    private String requiresDocuments;
}
