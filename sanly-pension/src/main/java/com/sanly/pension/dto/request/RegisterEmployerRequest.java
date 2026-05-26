package com.sanly.pension.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class RegisterEmployerRequest {
    @NotBlank private String businessName;
    @NotBlank private String businessRegistrationNumber;
    @NotBlank private String contactNationalId;
}
