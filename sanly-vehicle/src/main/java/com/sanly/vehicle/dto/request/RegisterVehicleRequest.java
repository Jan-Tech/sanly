package com.sanly.vehicle.dto.request;

import com.sanly.vehicle.entity.FuelType;
import com.sanly.vehicle.entity.VehicleType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterVehicleRequest {
    @NotBlank @Size(min = 17, max = 17) private String vin;
    @NotBlank private String make;
    @NotBlank private String model;
    @NotNull @Min(1900) @Max(2100) private Integer year;
    private String color;
    private String engineVolume;
    @NotNull private FuelType fuelType;
    @NotNull private VehicleType vehicleType;
    // Owner — exactly one of these must be provided
    private String ownerNationalId;
    private String ownerBusinessNumber;
}
