package com.sanly.court.dto.request;
import com.sanly.court.entity.CaseStatus;
public record UpdateCaseStatusRequest(CaseStatus status, String notes) {}
