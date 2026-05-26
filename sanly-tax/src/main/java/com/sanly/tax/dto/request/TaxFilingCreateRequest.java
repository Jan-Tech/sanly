package com.sanly.tax.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TaxFilingCreateRequest {
    @NotBlank private String taxId;
    @NotNull @Min(2000) @Max(2100) private Integer taxYear;
    @NotBlank private String declaredIncome;
    @NotBlank private String taxDue;
    private String taxPaid;
    private String notes;
}
