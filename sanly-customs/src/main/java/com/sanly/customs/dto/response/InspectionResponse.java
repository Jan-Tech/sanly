package com.sanly.customs.dto.response;

import com.sanly.customs.entity.InspectionRecord;
import com.sanly.customs.entity.InspectionResult;
import com.sanly.customs.entity.InspectionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InspectionResponse(
        UUID inspectionId, String declarationCode, UUID inspectorOfficerId,
        LocalDate inspectionDate, InspectionType inspectionType,
        String findings, InspectionResult result, String notes, LocalDateTime createdAt
) {
    public static InspectionResponse from(InspectionRecord r) {
        return new InspectionResponse(r.getInspectionId(), r.getDeclarationCode(), r.getInspectorOfficerId(),
                r.getInspectionDate(), r.getInspectionType(), r.getFindings(), r.getResult(),
                r.getNotes(), r.getCreatedAt());
    }
}
