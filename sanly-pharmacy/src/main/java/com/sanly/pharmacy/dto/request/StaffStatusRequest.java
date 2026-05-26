package com.sanly.pharmacy.dto.request;

import com.sanly.pharmacy.entity.StaffStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffStatusRequest {
    @NotNull private StaffStatus status;
}
