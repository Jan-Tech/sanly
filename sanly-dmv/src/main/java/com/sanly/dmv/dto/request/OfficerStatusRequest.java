package com.sanly.dmv.dto.request;

import com.sanly.dmv.entity.OfficerStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OfficerStatusRequest {
    @NotNull(message = "Status is required")
    private OfficerStatus status;
    @Size(max = 500)
    private String reason;
}
