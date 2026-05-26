package com.sanly.court.dto.response;

import com.sanly.court.entity.Court;
import com.sanly.court.entity.CourtStatus;
import com.sanly.court.entity.CourtType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourtResponse(
        UUID courtId, String courtCode, String name, CourtType courtType,
        String region, String address, CourtStatus status, LocalDateTime createdAt
) {
    public static CourtResponse from(Court c) {
        return new CourtResponse(c.getCourtId(), c.getCourtCode(), c.getName(), c.getCourtType(),
                c.getRegion(), c.getAddress(), c.getStatus(), c.getCreatedAt());
    }
}
