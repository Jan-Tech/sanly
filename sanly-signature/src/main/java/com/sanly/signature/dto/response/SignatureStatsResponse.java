package com.sanly.signature.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignatureStatsResponse {

    private long signedToday;
    private long verifiedToday;
    private long revokedTotal;
    private long totalSignatures;
}
