package com.sanly.social.dto.request;

import com.sanly.social.entity.BenefitType;

public record AutoTriggerRequest(
        String citizenNationalId,
        BenefitType benefitType,
        String programCode,
        String triggerReason
) {}
