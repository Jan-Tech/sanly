package com.sanly.dmv.client;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents one published data record returned by SANLY Bridge's
 * GET /api/v1/exchange/data/{nationalId}/{dataType} endpoint.
 */
@Data
public class BridgeRecord {

    private String id;
    private String publisherCode;
    private String nationalId;
    private String dataType;

    /** External record reference — used as visionTestRef on the issued license. */
    private String recordRef;

    /**
     * Non-sensitive metadata from the publishing institution.
     * For VISION_TEST, expected keys: "testType", "result", "testedAt", "clinicId".
     */
    private Map<String, Object> summary;

    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private boolean active;

    /** Convenience: extracts "result" from the summary map. */
    public String getSummaryResult() {
        if (summary == null) return null;
        Object v = summary.get("result");
        return v != null ? v.toString() : null;
    }

    /** True when expiresAt is non-null and in the past. */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
