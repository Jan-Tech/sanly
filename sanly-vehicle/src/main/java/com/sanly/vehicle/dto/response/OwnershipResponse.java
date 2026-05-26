package com.sanly.vehicle.dto.response;

import java.time.LocalDate;

public record OwnershipResponse(
        Long ownershipId,
        String plateNumber,
        String ownerNationalId,
        String ownerBusinessNumber,
        LocalDate ownershipStartDate,
        LocalDate ownershipEndDate,
        String acquiredVia,
        String status
) {}
