package com.sanly.analytics.dto.response;

import java.time.LocalDate;

public record TrendPoint(LocalDate date, double value) {}
