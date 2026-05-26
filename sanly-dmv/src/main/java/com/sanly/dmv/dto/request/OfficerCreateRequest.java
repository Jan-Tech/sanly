package com.sanly.dmv.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OfficerCreateRequest {

    @Pattern(regexp = "^\\d{11}$", message = "National ID must be 11 digits")
    private String nationalId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 150)
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 150)
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Size(max = 100, message = "Office region must not exceed 100 characters")
    private String officeRegion;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may only contain letters, digits, '.', '-', '_'")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128)
    private String password;
}
