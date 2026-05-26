package com.sanly.dmv.dto.request;

import com.sanly.dmv.entity.LicenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LicenseApplicationRequest {

    @NotBlank(message = "Citizen national ID is required")
    @Size(min = 5, max = 30)
    private String citizenNationalId;

    @NotNull(message = "Requested category is required")
    private LicenseCategory requestedCategory;
}
