package com.sanly.medical.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClinicCreateRequest {

    @NotBlank(message = "Clinic name is required")
    @Size(min = 3, max = 200, message = "Name must be 3–200 characters")
    private String name;

    @NotBlank(message = "License number is required")
    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String licenseNumber;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    @Pattern(regexp = "^[+\\d\\s()-]*$", message = "Invalid phone number format")
    private String phone;
}
