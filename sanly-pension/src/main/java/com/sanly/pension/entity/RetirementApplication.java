package com.sanly.pension.entity;

import com.sanly.pension.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "retirement_applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RetirementApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column(nullable = false, length = 25)
    private String accountCode;

    @Column(nullable = false, length = 20)
    private String citizenNationalId;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @Column(nullable = false)
    private LocalDate requestedStartDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    private Long processedByOfficerId;
    private LocalDateTime processedAt;

    @Column(length = 500)
    private String rejectionReason;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String monthlyPensionAmount;
}
