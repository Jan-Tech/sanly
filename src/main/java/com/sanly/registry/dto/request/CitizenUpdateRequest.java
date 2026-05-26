package com.sanly.registry.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CitizenUpdateRequest {

    @Size(min = 2, max = 150, message = "First name must be 2–150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "First name contains invalid characters")
    private String firstName;

    @Size(min = 2, max = 150, message = "Last name must be 2–150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Size(max = 150, message = "Middle name must not exceed 150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]*$", message = "Middle name contains invalid characters")
    private String middleName;

    @Size(max = 200, message = "Place of birth must not exceed 200 characters")
    private String placeOfBirth;

    @Valid
    private AddressRequest address;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    @Pattern(regexp = "^\\d{11}$", message = "Father ID must be 11 digits")
    private String fatherId;

    @Pattern(regexp = "^\\d{11}$", message = "Mother ID must be 11 digits")
    private String motherId;

    @Pattern(regexp = "^\\d{11}$", message = "Spouse ID must be 11 digits")
    private String spouseId;
}
