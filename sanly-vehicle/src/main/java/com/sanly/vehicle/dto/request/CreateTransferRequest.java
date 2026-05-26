package com.sanly.vehicle.dto.request;

import com.sanly.vehicle.entity.TransferType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTransferRequest {
    @NotBlank private String plateNumber;
    @NotBlank private String fromNationalId;
    private String toNationalId;
    private String toBusinessNumber;
    @NotNull private TransferType transferType;
    private String agreedPrice;
}
