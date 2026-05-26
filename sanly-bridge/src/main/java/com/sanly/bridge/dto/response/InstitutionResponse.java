package com.sanly.bridge.dto.response;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.InstitutionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class InstitutionResponse {
    private String institutionCode;
    private String name;
    private String description;
    private Set<DataType> publishableTypes;
    private InstitutionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
