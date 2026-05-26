package com.sanly.vehicle.dto.response;

import java.time.LocalDate;

public record VehicleVerifyResponse(
        String plateNumber,
        String vin,
        String make,
        String model,
        Integer year,
        String color,
        String vehicleType,
        String fuelType,
        String vehicleStatus,
        String ownerNationalId,
        String ownerBusinessNumber,
        String insuranceStatus,
        LocalDate insuranceValidUntil,
        LocalDate lastInspectionDate,
        LocalDate nextInspectionDue,
        String lastInspectionResult
) {}
