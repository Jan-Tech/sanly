package com.sanly.medical.dto.request;

import com.sanly.medical.entity.ClinicStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClinicStatusRequest {

    @NotNull(message = "Status is required")
    private ClinicStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
