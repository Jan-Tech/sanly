package com.sanly.pension.dto.response;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record PensionPaymentResponse(
        Long paymentId, String accountCode, String citizenNationalId,
        String paymentMonth, String amount, String status,
        LocalDate scheduledDate, LocalDateTime paidAt, String notes) {}
