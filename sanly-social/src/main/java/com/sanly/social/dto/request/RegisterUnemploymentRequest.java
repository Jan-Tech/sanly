package com.sanly.social.dto.request;

import com.sanly.social.entity.UnemploymentReason;

import java.time.LocalDate;

public record RegisterUnemploymentRequest(
        String citizenNationalId,
        String lastEmployer,
        LocalDate lastEmploymentDate,
        UnemploymentReason reason
) {}
