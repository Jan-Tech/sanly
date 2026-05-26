package com.sanly.pharmacy.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DispenseRequest {
    @NotBlank private String prescriptionCode;
    @NotNull @Min(1) private Integer quantityDispensed;
    private String notes;
}
