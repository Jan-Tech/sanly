package com.sanly.social.entity;

import com.sanly.social.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "benefit_claims")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class BenefitClaim {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID claimId;

    @Column(nullable = false, unique = true)
    private String claimCode; // TM-CLM-YYYYNNNNNN

    @Column(nullable = false) private String citizenNationalId;
    @Column(nullable = false) private String programCode;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ClaimStatus status;

    @Column(nullable = false) private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private UUID approvedByOfficerId;
    private LocalDate expiresAt;
    private String rejectionReason;

    @Convert(converter = EncryptedStringConverter.class)
    private String notes;

    private String triggerReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (appliedAt == null) appliedAt = createdAt;
        if (status == null) status = ClaimStatus.PENDING;
    }
}
