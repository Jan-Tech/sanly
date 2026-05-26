package com.sanly.pension.service;

import com.sanly.pension.dto.response.PensionPaymentResponse;
import com.sanly.pension.entity.PaymentStatus;
import com.sanly.pension.entity.PensionPayment;
import com.sanly.pension.exception.ResourceNotFoundException;
import com.sanly.pension.repository.PensionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PensionPaymentRepository repo;

    @Transactional(readOnly = true)
    public List<PensionPaymentResponse> findByAccount(String accountCode) {
        return repo.findByAccountCodeOrderByPaymentMonthDesc(accountCode)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PensionPaymentResponse> findScheduledThisMonth() {
        String month = currentMonth();
        return repo.findByStatusAndPaymentMonth(PaymentStatus.SCHEDULED, month)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PensionPaymentResponse markPaid(Long paymentId) {
        PensionPayment p = repo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        p.setStatus(PaymentStatus.PAID);
        p.setPaidAt(LocalDateTime.now());
        return toResponse(repo.save(p));
    }

    private String currentMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }

    private PensionPaymentResponse toResponse(PensionPayment p) {
        return new PensionPaymentResponse(p.getPaymentId(), p.getAccountCode(), p.getCitizenNationalId(),
                p.getPaymentMonth(), p.getAmount(), p.getStatus().name(),
                p.getScheduledDate(), p.getPaidAt(), p.getNotes());
    }
}
