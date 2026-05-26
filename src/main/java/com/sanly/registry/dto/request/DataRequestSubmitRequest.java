package com.sanly.registry.dto.request;

import com.sanly.registry.entity.AffectedService;
import com.sanly.registry.entity.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DataRequestSubmitRequest {

    @NotNull
    private RequestType requestType;

    @NotNull
    private AffectedService affectedService;

    @NotBlank
    @Size(min = 10, max = 2000)
    private String description;
}
