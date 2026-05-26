package com.sanly.registry.dto.request;

import com.sanly.registry.entity.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CitizenCreateRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 150, message = "First name must be 2–150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 150, message = "Last name must be 2–150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Size(max = 150, message = "Middle name must not exceed 150 characters")
    @Pattern(regexp = "^[\\p{L}\\s''-]*$", message = "Middle name contains invalid characters")
    private String middleName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Size(max = 200, message = "Place of birth must not exceed 200 characters")
    private String placeOfBirth;

    @Valid
    private AddressRequest address;

    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;

    @Pattern(regexp = "^\\d{11}$", message = "Father ID must be an 11-digit number")
    private String fatherId;

    @Pattern(regexp = "^\\d{11}$", message = "Mother ID must be an 11-digit number")
    private String motherId;

    @Pattern(regexp = "^\\d{11}$", message = "Spouse ID must be an 11-digit number")
    private String spouseId;

    @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone must be in E.164 format, e.g. +99361234567")
    private String phoneNumber;
}
