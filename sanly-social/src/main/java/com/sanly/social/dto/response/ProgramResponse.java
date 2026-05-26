package com.sanly.social.dto.response;

import com.sanly.social.entity.BenefitProgram;
import com.sanly.social.entity.BenefitType;
import com.sanly.social.entity.ProgramStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProgramResponse(
        UUID programId,
        String programCode,
        String name,
        String description,
        BenefitType benefitType,
        String monthlyAmount,
        String eligibilityCriteria,
        Integer maxDurationMonths,
        ProgramStatus status,
        LocalDateTime createdAt
) {
    public static ProgramResponse from(BenefitProgram p) {
        return new ProgramResponse(p.getProgramId(), p.getProgramCode(), p.getName(),
                p.getDescription(), p.getBenefitType(), p.getMonthlyAmount(),
                p.getEligibilityCriteria(), p.getMaxDurationMonths(), p.getStatus(), p.getCreatedAt());
    }
}
