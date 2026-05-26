package com.sanly.vehicle.dto.request;

import com.sanly.vehicle.entity.CoverageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterInsuranceRequest {
    @NotBlank private String plateNumber;
    @NotBlank private String insuranceCompany;
    @NotBlank private String policyNumber;
    @NotNull private CoverageType coverageType;
    @NotNull private String validFrom;
    @NotNull private String validUntil;
}
