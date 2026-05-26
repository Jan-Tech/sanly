package com.sanly.registry.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sanly.registry.entity.AffectedService;
import com.sanly.registry.entity.DataDeletionRequest;
import com.sanly.registry.entity.RequestStatus;
import com.sanly.registry.entity.RequestType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataRequestResponse {

    private UUID requestId;
    private String requestCode;
    private String citizenNationalId;
    private RequestType requestType;
    private AffectedService affectedService;
    private String description;
    private RequestStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByOfficerId;
    private String reviewerNotes;
    private String resolutionDescription;
    private boolean hasExport;

    public static DataRequestResponse toResponse(DataDeletionRequest r) {
        return DataRequestResponse.builder()
                .requestId(r.getRequestId())
                .requestCode(r.getRequestCode())
                .citizenNationalId(r.getCitizenNationalId())
                .requestType(r.getRequestType())
                .affectedService(r.getAffectedService())
                .description(r.getDescription())
                .status(r.getStatus())
                .submittedAt(r.getSubmittedAt())
                .reviewedAt(r.getReviewedAt())
                .reviewedByOfficerId(r.getReviewedByOfficerId())
                .reviewerNotes(r.getReviewerNotes())
                .resolutionDescription(r.getResolutionDescription())
                .hasExport(r.getExportData() != null)
                .build();
    }
}
