package com.sanly.social.entity;

import com.sanly.social.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pension_accounts")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class PensionAccount {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID accountId;

    @Column(nullable = false, unique = true)
    private String citizenNationalId;

    @Column(nullable = false) private LocalDate contributionStartDate;

    @Convert(converter = EncryptedStringConverter.class)
    private String totalContributions; // encrypted, stored as string amount

    // Calculated and stored: contributionStartDate + yearsRequired
    @Column(nullable = false) private LocalDate eligibleAt;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PensionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = PensionStatus.ACCUMULATING;
    }
}
