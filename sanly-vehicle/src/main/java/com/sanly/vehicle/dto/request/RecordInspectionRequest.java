package com.sanly.vehicle.dto.request;

import com.sanly.vehicle.entity.InspectionResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordInspectionRequest {
    @NotBlank private String plateNumber;
    @NotNull private String inspectionDate;
    @NotNull private String nextInspectionDue;
    @NotNull private InspectionResult result;
    private String findings;
}
