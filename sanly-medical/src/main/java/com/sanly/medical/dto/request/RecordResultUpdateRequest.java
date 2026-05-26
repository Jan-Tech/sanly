package com.sanly.medical.dto.request;

import com.sanly.medical.entity.RecordResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RecordResultUpdateRequest {

    @NotNull(message = "Result is required")
    private RecordResult result;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}
