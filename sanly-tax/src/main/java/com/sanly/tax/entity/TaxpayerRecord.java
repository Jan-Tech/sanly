package com.sanly.tax.entity;

import com.sanly.tax.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "taxpayer_records",
    indexes = {
        @Index(name = "idx_tr_citizen", columnList = "citizen_national_id"),
        @Index(name = "idx_tr_tax_id",  columnList = "tax_id")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxpayerRecord {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    @Column(name = "citizen_national_id", length = 30, nullable = false, unique = true)
    private String citizenNationalId;

    @Column(name = "tax_id", length = 20, nullable = false, unique = true)
    private String taxId;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "taxpayer_type", length = 15, nullable = false)
    private TaxpayerType taxpayerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private TaxpayerStatus status = TaxpayerStatus.ACTIVE;

    /** Encrypted stored value: LOW, MEDIUM, or HIGH */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "annual_income_class", columnDefinition = "TEXT")
    private String annualIncomeClass;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "marital_status", length = 15)
    private MaritalStatus maritalStatus = MaritalStatus.SINGLE;

    @Column(name = "spouse_national_id", length = 11)
    private String spouseNationalId;

    @Column(name = "registered_by_officer_id", nullable = false)
    private Long registeredByOfficerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
