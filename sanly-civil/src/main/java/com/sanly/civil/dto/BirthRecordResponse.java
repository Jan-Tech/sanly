package com.sanly.civil.dto;

import com.sanly.civil.entity.BirthRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BirthRecordResponse(UUID id, String certificateNumber, String childNationalId,
                                   String childFirstName, String childLastName, LocalDate dateOfBirth,
                                   String placeOfBirth, String fatherNationalId, String fatherFullName,
                                   String motherNationalId, String motherFullName, Instant createdAt) {
    public static BirthRecordResponse from(BirthRecord r) {
        return new BirthRecordResponse(r.getId(), r.getCertificateNumber(), r.getChildNationalId(),
                r.getChildFirstName(), r.getChildLastName(), r.getDateOfBirth(), r.getPlaceOfBirth(),
                r.getFatherNationalId(), r.getFatherFullName(), r.getMotherNationalId(),
                r.getMotherFullName(), r.getCreatedAt());
    }
}
