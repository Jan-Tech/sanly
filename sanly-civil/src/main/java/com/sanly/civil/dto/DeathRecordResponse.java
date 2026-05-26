package com.sanly.civil.dto;

import com.sanly.civil.entity.DeathRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DeathRecordResponse(UUID id, String certificateNumber, String deceasedNationalId,
                                   String deceasedFullName, LocalDate dateOfDeath, String placeOfDeath,
                                   String deathCause, Instant createdAt) {
    public static DeathRecordResponse from(DeathRecord r) {
        return new DeathRecordResponse(r.getId(), r.getCertificateNumber(), r.getDeceasedNationalId(),
                r.getDeceasedFullName(), r.getDateOfDeath(), r.getPlaceOfDeath(),
                r.getDeathCause(), r.getCreatedAt());
    }
}
