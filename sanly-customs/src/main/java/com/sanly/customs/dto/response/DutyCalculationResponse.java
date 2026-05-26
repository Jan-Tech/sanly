package com.sanly.customs.dto.response;

import com.sanly.customs.entity.DutyCalculation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DutyCalculationResponse(
        UUID calculationId, String declarationCode, String hsCode,
        BigDecimal dutyRatePercent, BigDecimal vatRatePercent,
        String calculatedDutyAmount, String vatAmount, String totalOwed,
        String dutyRateOverride, LocalDateTime calculatedAt
) {
    public static DutyCalculationResponse from(DutyCalculation d) {
        return new DutyCalculationResponse(d.getCalculationId(), d.getDeclarationCode(), d.getHsCode(),
                d.getDutyRatePercent(), d.getVatRatePercent(),
                d.getCalculatedDutyAmount(), d.getVatAmount(), d.getTotalOwed(),
                d.getDutyRateOverride(), d.getCalculatedAt());
    }
}
