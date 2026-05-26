package com.sanly.bridge.dto.response;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.ExchangeResult;
import com.sanly.bridge.entity.OperationType;
import com.sanly.bridge.entity.QueryPurpose;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExchangeLogResponse {
    private Long id;
    private String requestingCode;
    private String targetCode;
    private String nationalId;
    private DataType dataType;
    private OperationType operationType;
    private ExchangeResult result;
    private Integer responseTimeMs;
    private String details;
    private LocalDateTime exchangedAt;

    // ── Purpose Code fields — visible to citizens in their access log ─────────

    /** Declared reason for this query. Null for PUBLISH operations. */
    private QueryPurpose purposeCode;

    /**
     * Case reference if provided. Citizens can see this — it answers WHY
     * an institution accessed their data (e.g. "COURT-ORDER-2024-1234").
     */
    private String caseReference;
}
