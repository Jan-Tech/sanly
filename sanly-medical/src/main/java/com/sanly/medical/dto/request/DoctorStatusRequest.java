package com.sanly.medical.dto.request;

import com.sanly.medical.entity.DoctorStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorStatusRequest {

    @NotNull(message = "Status is required")
    private DoctorStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
