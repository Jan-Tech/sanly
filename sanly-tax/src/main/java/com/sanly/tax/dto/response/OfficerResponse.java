package com.sanly.tax.dto.response;

import com.sanly.tax.entity.OfficerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class OfficerResponse {
    private Long officerId;
    private String nationalId, firstName, lastName, officeRegion;
    private OfficerStatus status;
    private LocalDateTime createdAt;
}
