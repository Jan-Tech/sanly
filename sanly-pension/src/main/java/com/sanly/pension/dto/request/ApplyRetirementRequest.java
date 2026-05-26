package com.sanly.pension.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class ApplyRetirementRequest {
    @NotBlank private String citizenNationalId;
    @NotNull  private String requestedStartDate;
}
