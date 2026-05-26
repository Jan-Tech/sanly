package com.sanly.police.dto.response;

import com.sanly.police.entity.OfficerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class OfficerResponse {
    private Long officerId;
    private String nationalId, firstName, lastName, badgeNumber, rank, stationRegion;
    private OfficerStatus status;
    private LocalDateTime createdAt;
}
