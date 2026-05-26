package com.sanly.bridge.dto.request;

import com.sanly.bridge.entity.AlertStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewAnomalyRequest {

    @NotNull(message = "status is required (REVIEWED or DISMISSED)")
    private AlertStatus status;

    @NotBlank(message = "reviewedBy is required")
    private String reviewedBy;
}
