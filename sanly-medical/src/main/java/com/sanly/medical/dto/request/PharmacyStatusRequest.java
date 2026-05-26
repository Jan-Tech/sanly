package com.sanly.medical.dto.request;

import com.sanly.medical.entity.PharmacyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PharmacyStatusRequest {
    @NotNull
    private PharmacyStatus status;
}
