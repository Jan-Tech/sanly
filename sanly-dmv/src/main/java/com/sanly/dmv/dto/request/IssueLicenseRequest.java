package com.sanly.dmv.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class IssueLicenseRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
