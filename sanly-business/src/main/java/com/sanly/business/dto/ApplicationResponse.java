package com.sanly.business.dto;

import com.sanly.business.entity.ApplicationStatus;
import com.sanly.business.entity.BusinessType;
import com.sanly.business.entity.RegistrationApplication;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(UUID id, String applicantNationalId, String applicantFullName,
                                   String proposedBusinessName, BusinessType businessType,
                                   String proposedAddress, ApplicationStatus status,
                                   String rejectionReason, String taxCheckResult,
                                   String criminalCheckResult, UUID approvedBusinessId,
                                   Instant createdAt, Instant updatedAt) {
    public static ApplicationResponse from(RegistrationApplication a) {
        return new ApplicationResponse(a.getId(), a.getApplicantNationalId(), a.getApplicantFullName(),
                a.getProposedBusinessName(), a.getBusinessType(), a.getProposedAddress(),
                a.getStatus(), a.getRejectionReason(), a.getTaxCheckResult(),
                a.getCriminalCheckResult(), a.getApprovedBusinessId(),
                a.getCreatedAt(), a.getUpdatedAt());
    }
}
