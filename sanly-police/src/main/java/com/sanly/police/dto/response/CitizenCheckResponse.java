package com.sanly.police.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.police.entity.CheckType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class CitizenCheckResponse {
    private UUID checkId;
    private String citizenNationalId;
    private Long checkedByOfficerId;
    private CheckType checkType;
    private JsonNode bridgeResults;
    private LocalDateTime performedAt;
}
