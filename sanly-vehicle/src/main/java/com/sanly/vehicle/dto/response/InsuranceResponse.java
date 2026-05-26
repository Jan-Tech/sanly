package com.sanly.vehicle.dto.response;

import java.time.LocalDate;

public record InsuranceResponse(
        Long insuranceId,
        String plateNumber,
        String insuranceCompany,
        String coverageType,
        LocalDate validFrom,
        LocalDate validUntil,
        String status,
        Long registeredByOfficerId
) {}
