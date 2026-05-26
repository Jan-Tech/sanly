package com.sanly.bridge.dto.request;

import com.sanly.bridge.entity.InstitutionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionStatusRequest {

    @NotNull(message = "Status is required")
    private InstitutionStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
