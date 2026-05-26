package com.sanly.vehicle.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeceasedOwnershipRequest {
    @NotBlank private String deceasedNationalId;
}
