package com.sanly.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    private long consentRequestsToday;
    private long approvedToday;
    private long rejectedToday;
    private long expiredToday;
    private long totalActiveBanks;
}
