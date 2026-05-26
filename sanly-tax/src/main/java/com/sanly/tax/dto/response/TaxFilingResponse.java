package com.sanly.tax.dto.response;

import com.sanly.tax.entity.FilingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class TaxFilingResponse {
    private UUID filingId;
    private String taxId;
    private Integer taxYear;
    private FilingStatus filingStatus;
    private String declaredIncome, taxDue, taxPaid;
    private LocalDateTime submittedAt, processedAt;
    private String notes;
}
