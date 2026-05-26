package com.sanly.court.dto.request;
import java.time.LocalDate;
public record IssueFineRequest(
        String caseNumber,
        String citizenNationalId,
        String amount,
        String reason,
        LocalDate dueDate,
        String notes
) {}
