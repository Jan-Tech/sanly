package com.sanly.social.dto.request;

import java.time.LocalDate;

public record OpenPensionRequest(
        String citizenNationalId,
        LocalDate contributionStartDate
) {}
