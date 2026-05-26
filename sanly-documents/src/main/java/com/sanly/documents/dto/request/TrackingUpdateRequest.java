package com.sanly.documents.dto.request;

import com.sanly.documents.entity.ItemType;
import com.sanly.documents.entity.SourceService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TrackingUpdateRequest {

    @NotBlank(message = "Citizen national ID is required")
    private String citizenNationalId;

    @NotNull(message = "Item type is required")
    private ItemType itemType;

    @NotNull(message = "Source service is required")
    private SourceService sourceService;

    @NotBlank(message = "Source item code is required")
    private String sourceItemCode;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Current status is required")
    private String currentStatus;

    private String statusDescription;

    private boolean isCompleted;
}
