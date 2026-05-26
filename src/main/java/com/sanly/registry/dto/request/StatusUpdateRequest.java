package com.sanly.registry.dto.request;

import com.sanly.registry.entity.CitizenStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CitizenStatus status;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
