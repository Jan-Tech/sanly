package com.sanly.tax.entity;

import com.sanly.tax.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tax_filings",
    indexes = {
        @Index(name = "idx_tf_tax_id", columnList = "tax_id"),
        @Index(name = "idx_tf_status", columnList = "filing_status"),
        @Index(name = "idx_tf_year",   columnList = "tax_year")
    },
    uniqueConstraints = @UniqueConstraint(name = "uq_filing_year", columnNames = {"tax_id", "tax_year"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxFiling {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID filingId;

    @Column(name = "tax_id", length = 20, nullable = false)
    private String taxId;

    @Column(name = "tax_year", nullable = false)
    private Integer taxYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "filing_status", length = 15, nullable = false)
    @Builder.Default
    private FilingStatus filingStatus = FilingStatus.SUBMITTED;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "declared_income", columnDefinition = "TEXT")
    private String declaredIncome;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "tax_due", columnDefinition = "TEXT")
    private String taxDue;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "tax_paid", columnDefinition = "TEXT")
    private String taxPaid;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by_officer_id")
    private Long processedByOfficerId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
