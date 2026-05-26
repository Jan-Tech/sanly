package com.sanly.customs.dto.request;

import com.sanly.customs.entity.OfficerRole;

public record CreateOfficerRequest(
        String nationalId,
        String firstName,
        String lastName,
        String username,
        String password,
        String port,
        OfficerRole role
) {}
