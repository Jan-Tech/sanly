package com.sanly.pension.dto.request;
import com.sanly.pension.entity.ContributionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class VerifyContributionRequest {
    @NotNull private ContributionStatus status;
    private String rejectionReason;
}
