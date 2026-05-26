package com.sanly.social.service;

import com.sanly.social.dto.response.PaymentResponse;
import com.sanly.social.entity.BenefitClaim;
import com.sanly.social.entity.BenefitPayment;
import com.sanly.social.entity.BenefitProgram;
import com.sanly.social.entity.PaymentStatus;
import com.sanly.social.exception.RecordNotFoundException;
import com.sanly.social.repository.BenefitPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BenefitPaymentService {

    private final BenefitPaymentRepository paymentRepo;

    @Transactional
    public BenefitPayment schedulePayment(BenefitClaim claim, BenefitProgram program) {
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if (paymentRepo.existsByClaimCodeAndPaymentPeriod(claim.getClaimCode(), period))
            return null; // already scheduled for this period

        BenefitPayment payment = BenefitPayment.builder()
                .claimCode(claim.getClaimCode())
                .citizenNationalId(claim.getCitizenNationalId())
                .paymentPeriod(period)
                .amount(program.getMonthlyAmount())
                .status(PaymentStatus.SCHEDULED)
                .scheduledDate(LocalDate.now().withDayOfMonth(1).plusMonths(1))
                .build();
        return paymentRepo.save(payment);
    }

    @Transactional
    public BenefitPayment schedulePaymentForPeriod(BenefitClaim claim, String amount, String period) {
        if (paymentRepo.existsByClaimCodeAndPaymentPeriod(claim.getClaimCode(), period))
            return null;

        BenefitPayment payment = BenefitPayment.builder()
                .claimCode(claim.getClaimCode())
                .citizenNationalId(claim.getCitizenNationalId())
                .paymentPeriod(period)
                .amount(amount)
                .status(PaymentStatus.SCHEDULED)
                .scheduledDate(LocalDate.parse(period + "-01").plusMonths(1).withDayOfMonth(1))
                .build();
        return paymentRepo.save(payment);
    }

    @Transactional
    public PaymentResponse markPaid(UUID paymentId) {
        BenefitPayment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RecordNotFoundException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.SCHEDULED)
            throw new com.sanly.social.exception.InvalidOperationException("Payment is not SCHEDULED: " + paymentId);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return PaymentResponse.from(paymentRepo.save(payment));
    }

    public List<PaymentResponse> findByClaimCode(String claimCode) {
        return paymentRepo.findByClaimCode(claimCode).stream().map(PaymentResponse::from).toList();
    }

    public List<PaymentResponse> findByCitizen(String nationalId) {
        return paymentRepo.findByCitizenNationalId(nationalId).stream().map(PaymentResponse::from).toList();
    }

    public List<PaymentResponse> findScheduledThisMonth() {
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return paymentRepo.findScheduledForPeriod(period).stream().map(PaymentResponse::from).toList();
    }
}
