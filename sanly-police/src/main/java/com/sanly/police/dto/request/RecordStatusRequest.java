package com.sanly.police.dto.request;

import com.sanly.police.entity.RecordStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordStatusRequest {
    @NotNull private RecordStatus status;
    private String reason;
}
