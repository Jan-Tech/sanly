package com.sanly.tax.dto.request;

import com.sanly.tax.entity.FilingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FilingProcessRequest {
    /** Must be ACCEPTED or REJECTED — other statuses are not valid for this endpoint. */
    @NotNull private FilingStatus filingStatus;
    private String notes;
}
