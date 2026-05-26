package com.sanly.bridge.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.sanly.bridge.entity.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExchangePublishRequest {

    @NotBlank(message = "National ID is required")
    @Size(min = 5, max = 30, message = "National ID must be 5–30 characters")
    private String nationalId;

    @NotNull(message = "Data type is required")
    private DataType dataType;

    /**
     * Reference ID of this record in the publisher's own system.
     * Other institutions use this to retrieve full details from the source.
     */
    @Size(max = 500, message = "Record reference must not exceed 500 characters")
    private String recordRef;

    /**
     * Non-sensitive metadata summary. Must not contain raw PII.
     * Example: {"passed": true, "date": "2024-01-15", "result": "CLEAR"}
     */
    private JsonNode summary;

    /** When null this record never expires. */
    private LocalDateTime expiresAt;
}
