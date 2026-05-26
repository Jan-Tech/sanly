package com.sanly.customs.dto.request;

import java.math.BigDecimal;

public record CalculateDutiesRequest(
        String dutyRateOverride,   // optional: officer override justification
        BigDecimal overrideRatePercent  // optional: manual rate to apply
) {}
