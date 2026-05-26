package com.sanly.medical.dto.request;

import com.sanly.medical.entity.TestType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicalRecordCreateRequest {

    @NotBlank(message = "Citizen national ID is required")
    @Size(min = 5, max = 30, message = "National ID must be 5–30 characters")
    private String citizenNationalId;

    @NotNull(message = "Test type is required")
    private TestType testType;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    @NotNull(message = "Test date/time is required")
    @PastOrPresent(message = "Test date must be in the past or present")
    private LocalDateTime testedAt;

    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiresAt;
}
