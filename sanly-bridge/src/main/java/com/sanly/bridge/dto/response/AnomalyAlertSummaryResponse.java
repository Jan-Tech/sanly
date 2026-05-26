package com.sanly.bridge.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnomalyAlertSummaryResponse {
    private long totalOpen;
    private Map<String, Long> openBySeverity;
}
