package com.sanly.vehicle.dto.response;

import java.time.LocalDate;

public record InspectionResponse(
        Long inspectionId,
        String plateNumber,
        LocalDate inspectionDate,
        LocalDate nextInspectionDue,
        String result,
        Long inspectedByOfficerId
) {}
