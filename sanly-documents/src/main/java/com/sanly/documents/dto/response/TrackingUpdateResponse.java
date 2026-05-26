package com.sanly.documents.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackingUpdateResponse {

    private UUID updateId;
    private String status;
    private String description;
    private LocalDateTime updatedAt;
    private String updatedByService;
}
