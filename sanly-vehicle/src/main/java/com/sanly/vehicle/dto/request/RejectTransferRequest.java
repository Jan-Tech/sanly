package com.sanly.vehicle.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectTransferRequest {
    @NotBlank private String rejectionReason;
}
