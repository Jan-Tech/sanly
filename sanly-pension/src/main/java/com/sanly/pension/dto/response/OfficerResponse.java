package com.sanly.pension.dto.response;
public record OfficerResponse(
        Long officerId, String nationalId, String firstName, String lastName,
        String username, String region, String role, String status, String employerCode) {}
