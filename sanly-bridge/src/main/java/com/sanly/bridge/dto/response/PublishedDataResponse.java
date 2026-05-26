package com.sanly.bridge.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.bridge.entity.DataType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PublishedDataResponse {
    private UUID id;
    private String publisherCode;
    private String nationalId;
    private DataType dataType;
    private String recordRef;
    private JsonNode summary;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private boolean active;
}
