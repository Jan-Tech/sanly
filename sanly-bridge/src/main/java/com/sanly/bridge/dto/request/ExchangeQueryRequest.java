package com.sanly.bridge.dto.request;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.QueryPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExchangeQueryRequest {

    @NotBlank(message = "National ID is required")
    @Size(min = 5, max = 30, message = "National ID must be 5–30 characters")
    private String nationalId;

    @NotNull(message = "Data type is required")
    private DataType dataType;

    /**
     * Optionally narrow the search to a specific publisher institution.
     * If null, the bridge searches all institutions that have published
     * this data type for the given citizen.
     */
    @Pattern(regexp = "^INST_[A-Z0-9_]+$", message = "Invalid institution code format")
    private String targetCode;

    // ── Purpose Code fields (Anti-Corruption Feature 1) ──────────────────────

    /** Required — every query must declare its purpose. Rejected with 400 if absent. */
    @NotNull(message = "purposeCode is required")
    private QueryPurpose purposeCode;

    /**
     * Court order number, case number, traffic stop reference, etc.
     * Required for CRIMINAL_RECORD and MEDICAL_CLEARANCE data types.
     * Visible to citizens when they view their own access log.
     */
    @Size(max = 100, message = "caseReference must not exceed 100 characters")
    private String caseReference;

    /** Optional free-text justification. */
    @Size(max = 500, message = "justification must not exceed 500 characters")
    private String justification;
}
