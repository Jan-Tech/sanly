package com.sanly.vehicle.dto.response;

import java.time.LocalDate;

public record InsuranceVerifyResponse(
        String plateNumber,
        boolean insured,
        String insuranceCompany,
        String coverageType,
        LocalDate validUntil
) {}
