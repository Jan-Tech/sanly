package com.sanly.customs.dto.request;

import com.sanly.customs.entity.InspectionResult;
import com.sanly.customs.entity.InspectionType;

import java.time.LocalDate;

public record RecordInspectionRequest(
        InspectionType inspectionType,
        LocalDate inspectionDate,
        String findings,
        InspectionResult result,
        String notes
) {}
