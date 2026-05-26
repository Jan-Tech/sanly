package com.sanly.documents.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocStatsResponse {

    private long certsGeneratedToday;
    private long verificationsToday;
    private long activeCerts;
    private String topDocumentType;
}
