package com.sanly.customs.dto.response;

import com.sanly.customs.entity.CustomsPort;
import com.sanly.customs.entity.PortStatus;
import com.sanly.customs.entity.PortType;

import java.time.LocalDateTime;
import java.util.UUID;

public record PortResponse(
        UUID portId, String portCode, String name, PortType portType,
        String region, String address, PortStatus status, LocalDateTime createdAt
) {
    public static PortResponse from(CustomsPort p) {
        return new PortResponse(p.getPortId(), p.getPortCode(), p.getName(), p.getPortType(),
                p.getRegion(), p.getAddress(), p.getStatus(), p.getCreatedAt());
    }
}
