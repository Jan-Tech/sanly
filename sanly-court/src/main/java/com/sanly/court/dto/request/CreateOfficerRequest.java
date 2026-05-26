package com.sanly.court.dto.request;
import com.sanly.court.entity.OfficerRole;
public record CreateOfficerRequest(
        String nationalId, String firstName, String lastName,
        String username, String password, String courtCode, OfficerRole role
) {}
