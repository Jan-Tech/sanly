package com.sanly.dmv.dto.request;

import com.sanly.dmv.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProcessApplicationRequest {

    /** Must be APPROVED or REJECTED. PENDING is not a valid transition target. */
    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    private String rejectionReason;
}
