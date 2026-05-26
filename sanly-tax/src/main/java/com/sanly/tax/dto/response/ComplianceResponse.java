package com.sanly.tax.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ComplianceResponse {
    private String nationalId;
    private String taxId;
    /** COMPLIANT, NON_COMPLIANT, or PENDING (no filings yet) */
    private String complianceStatus;
    private Integer latestFilingYear;
    private String latestFilingStatus;
}
