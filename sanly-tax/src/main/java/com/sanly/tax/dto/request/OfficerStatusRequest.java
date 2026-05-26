package com.sanly.tax.dto.request;

import com.sanly.tax.entity.OfficerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfficerStatusRequest {
    @NotNull private OfficerStatus status;
}
