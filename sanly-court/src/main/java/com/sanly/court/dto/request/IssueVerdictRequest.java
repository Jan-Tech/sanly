package com.sanly.court.dto.request;
import com.sanly.court.entity.VerdictType;
public record IssueVerdictRequest(VerdictType verdictType, String summary) {}
