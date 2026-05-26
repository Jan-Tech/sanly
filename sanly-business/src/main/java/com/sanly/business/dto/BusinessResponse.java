package com.sanly.business.dto;

import com.sanly.business.entity.Business;
import com.sanly.business.entity.BusinessStatus;
import com.sanly.business.entity.BusinessType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BusinessResponse(UUID id, String registrationNumber, String businessName,
                                BusinessType businessType, BusinessStatus status,
                                String ownerNationalId, String ownerFullName,
                                String address, LocalDate registrationDate,
                                LocalDate expiryDate, Instant createdAt) {
    public static BusinessResponse from(Business b) {
        return new BusinessResponse(b.getId(), b.getRegistrationNumber(), b.getBusinessName(),
                b.getBusinessType(), b.getStatus(), b.getOwnerNationalId(), b.getOwnerFullName(),
                b.getAddress(), b.getRegistrationDate(), b.getExpiryDate(), b.getCreatedAt());
    }
}
