package com.sanly.pension.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class RejectRetirementRequest {
    @NotBlank private String rejectionReason;
}
