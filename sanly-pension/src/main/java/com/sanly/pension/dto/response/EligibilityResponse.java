package com.sanly.pension.dto.response;
import java.math.BigDecimal;
import java.time.LocalDate;
public record EligibilityResponse(
        boolean eligible,
        LocalDate eligibleAt,
        int yearsRemaining,
        long monthsContributed,
        BigDecimal projectedMonthlyAmount) {}
