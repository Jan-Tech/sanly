package com.sanly.police.dto.request;

import com.sanly.police.entity.OfficerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfficerStatusRequest {
    @NotNull private OfficerStatus status;
}
