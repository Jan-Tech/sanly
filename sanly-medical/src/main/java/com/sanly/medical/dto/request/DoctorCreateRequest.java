package com.sanly.medical.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DoctorCreateRequest {

    @Pattern(regexp = "^\\d{11}$", message = "National ID must be 11 digits")
    private String nationalId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 150, message = "First name must be 2–150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 150, message = "Last name must be 2–150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Size(max = 200, message = "Specialization must not exceed 200 characters")
    private String specialization;

    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String licenseNumber;

    @NotNull(message = "Clinic ID is required")
    @Positive(message = "Clinic ID must be a positive number")
    private Long clinicId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be 3–100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may only contain letters, digits, '.', '-', '_'")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be 8–128 characters")
    private String password;
}
