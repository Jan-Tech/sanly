package com.sanly.social.dto.request;

import com.sanly.social.entity.OfficerRole;

public record CreateOfficerRequest(
        String nationalId,
        String username,
        String password,
        String region,
        OfficerRole role
) {}
