package com.sanly.medical.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DispensePrescriptionRequest {

    @NotNull
    @Min(value = 1, message = "quantityDispensed must be >= 1")
    private Integer quantityDispensed;

    private String pharmacistNationalId;

    private String notes;
}
