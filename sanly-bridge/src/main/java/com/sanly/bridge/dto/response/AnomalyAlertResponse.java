package com.sanly.bridge.dto.response;

import com.sanly.bridge.entity.AlertSeverity;
import com.sanly.bridge.entity.AlertStatus;
import com.sanly.bridge.entity.AnomalyAlert;
import com.sanly.bridge.entity.AnomalyType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AnomalyAlertResponse {
    private UUID alertId;
    private String institutionCode;
    private AnomalyType alertType;
    private String description;
    private AlertSeverity severity;
    private String nationalIdInvolved;
    private LocalDateTime detectedAt;
    private AlertStatus status;
    private String reviewedBy;
    private LocalDateTime reviewedAt;

    public static AnomalyAlertResponse from(AnomalyAlert a) {
        return AnomalyAlertResponse.builder()
                .alertId(a.getAlertId())
                .institutionCode(a.getInstitutionCode())
                .alertType(a.getAlertType())
                .description(a.getDescription())
                .severity(a.getSeverity())
                .nationalIdInvolved(a.getNationalIdInvolved())
                .detectedAt(a.getDetectedAt())
                .status(a.getStatus())
                .reviewedBy(a.getReviewedBy())
                .reviewedAt(a.getReviewedAt())
                .build();
    }
}
