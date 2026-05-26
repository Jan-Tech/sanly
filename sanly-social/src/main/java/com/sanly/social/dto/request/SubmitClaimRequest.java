package com.sanly.social.dto.request;

public record SubmitClaimRequest(
        String citizenNationalId,
        String programCode,
        String notes
) {}
