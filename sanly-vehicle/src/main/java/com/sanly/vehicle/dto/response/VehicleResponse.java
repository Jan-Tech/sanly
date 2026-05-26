package com.sanly.vehicle.dto.response;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long vehicleId,
        String plateNumber,
        String vin,
        String make,
        String model,
        Integer year,
        String color,
        String engineVolume,
        String fuelType,
        String vehicleType,
        LocalDateTime registeredAt,
        String status
) {}
