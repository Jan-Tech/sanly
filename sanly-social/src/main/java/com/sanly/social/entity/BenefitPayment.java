package com.sanly.social.entity;

import com.sanly.social.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "benefit_payments")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class BenefitPayment {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    @Column(nullable = false) private String claimCode;
    @Column(nullable = false) private String citizenNationalId;
    @Column(nullable = false) private String paymentPeriod; // YYYY-MM

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String amount; // encrypted

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false) private LocalDate scheduledDate;
    private LocalDateTime paidAt;

    @Convert(converter = EncryptedStringConverter.class)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = PaymentStatus.SCHEDULED;
    }
}
