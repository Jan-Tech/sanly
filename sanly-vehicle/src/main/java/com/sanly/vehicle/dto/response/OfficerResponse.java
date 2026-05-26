package com.sanly.vehicle.dto.response;

public record OfficerResponse(
        Long officerId,
        String nationalId,
        String firstName,
        String lastName,
        String username,
        String region,
        String role,
        String status
) {}
