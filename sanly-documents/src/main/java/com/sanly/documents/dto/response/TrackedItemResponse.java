package com.sanly.documents.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sanly.documents.entity.ItemType;
import com.sanly.documents.entity.SourceService;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackedItemResponse {

    private UUID trackingId;
    private String trackingCode;
    private String citizenNationalId;
    private ItemType itemType;
    private SourceService sourceService;
    private String sourceItemCode;
    private String title;
    private String currentStatus;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private LocalDateTime completedAt;
    private boolean isCompleted;
    private List<TrackingUpdateResponse> updates;
}
