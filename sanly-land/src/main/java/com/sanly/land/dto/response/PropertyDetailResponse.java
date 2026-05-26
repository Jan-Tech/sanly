package com.sanly.land.dto.response;

import java.util.List;

public record PropertyDetailResponse(
        PropertyResponse property,
        List<OwnershipResponse> owners
) {}
