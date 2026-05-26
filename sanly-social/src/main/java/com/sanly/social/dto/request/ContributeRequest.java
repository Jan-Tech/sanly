package com.sanly.social.dto.request;

import java.time.LocalDate;

public record ContributeRequest(String amount, LocalDate contributionDate) {}
