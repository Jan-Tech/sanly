package com.sanly.pension.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class SubmitContributionRequest {
    @NotBlank private String accountCode;
    private String employerCode;
    @NotBlank private String contributionMonth;
    private String employerAmount;
    private String citizenAmount;
    @NotBlank private String totalAmount;
}
