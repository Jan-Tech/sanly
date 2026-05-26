package com.sanly.pension.entity;

import com.sanly.pension.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pension_payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PensionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false, length = 25)
    private String accountCode;

    @Column(nullable = false, length = 20)
    private String citizenNationalId;

    @Column(nullable = false, length = 7)
    private String paymentMonth;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    private LocalDateTime paidAt;

    @Column(length = 500)
    private String notes;
}
