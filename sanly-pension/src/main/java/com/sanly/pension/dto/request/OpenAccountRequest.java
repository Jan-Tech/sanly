package com.sanly.pension.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class OpenAccountRequest {
    @NotBlank private String citizenNationalId;
    @NotNull  private String employmentStartDate;
    /** Optional — officer may provide birthDate if not available from registry */
    private String birthDate;
}
