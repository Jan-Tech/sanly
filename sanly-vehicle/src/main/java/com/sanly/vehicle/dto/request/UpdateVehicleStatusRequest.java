package com.sanly.vehicle.dto.request;

import com.sanly.vehicle.entity.VehicleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVehicleStatusRequest {
    @NotNull private VehicleStatus status;
    private String reason;
}
