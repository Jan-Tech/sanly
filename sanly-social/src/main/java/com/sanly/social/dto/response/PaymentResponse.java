package com.sanly.social.dto.response;

import com.sanly.social.entity.BenefitPayment;
import com.sanly.social.entity.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String claimCode,
        String citizenNationalId,
        String paymentPeriod,
        String amount,
        PaymentStatus status,
        LocalDate scheduledDate,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(BenefitPayment p) {
        return new PaymentResponse(p.getPaymentId(), p.getClaimCode(), p.getCitizenNationalId(),
                p.getPaymentPeriod(), p.getAmount(), p.getStatus(),
                p.getScheduledDate(), p.getPaidAt(), p.getCreatedAt());
    }
}
