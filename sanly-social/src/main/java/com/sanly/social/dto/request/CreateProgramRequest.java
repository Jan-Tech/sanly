package com.sanly.social.dto.request;

import com.sanly.social.entity.BenefitType;

public record CreateProgramRequest(
        String name,
        String description,
        BenefitType benefitType,
        String monthlyAmount,
        String eligibilityCriteria,
        Integer maxDurationMonths
) {}
