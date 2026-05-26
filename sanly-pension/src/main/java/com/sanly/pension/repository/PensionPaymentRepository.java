package com.sanly.pension.repository;

import com.sanly.pension.entity.PaymentStatus;
import com.sanly.pension.entity.PensionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PensionPaymentRepository extends JpaRepository<PensionPayment, Long> {
    List<PensionPayment> findByAccountCodeOrderByPaymentMonthDesc(String accountCode);
    List<PensionPayment> findByStatusAndPaymentMonth(PaymentStatus status, String paymentMonth);
    boolean existsByAccountCodeAndPaymentMonth(String accountCode, String paymentMonth);
}
