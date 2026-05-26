package com.sanly.customs.dto.request;

import com.sanly.customs.entity.PortType;

public record CreatePortRequest(
        String name,
        PortType portType,
        String region,
        String address
) {}
