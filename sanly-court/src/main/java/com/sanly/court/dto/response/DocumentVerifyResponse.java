package com.sanly.court.dto.response;

import java.time.LocalDateTime;

public record DocumentVerifyResponse(
        boolean valid,
        String documentCode,
        String submittedBy,
        LocalDateTime submittedAt,
        String status,
        String message
) {}
