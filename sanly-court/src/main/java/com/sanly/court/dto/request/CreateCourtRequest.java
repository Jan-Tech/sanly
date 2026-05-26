package com.sanly.court.dto.request;
import com.sanly.court.entity.CourtType;
public record CreateCourtRequest(String name, CourtType courtType, String region, String address) {}
