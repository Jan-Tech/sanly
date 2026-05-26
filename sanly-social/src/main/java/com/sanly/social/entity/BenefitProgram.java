package com.sanly.social.entity;

import com.sanly.social.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "benefit_programs")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class BenefitProgram {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID programId;

    @Column(nullable = false, unique = true)
    private String programCode; // TM-BEN-NNNN

    @Column(nullable = false) private String name;
    private String description;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private BenefitType benefitType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String monthlyAmount; // encrypted

    @Column(columnDefinition = "TEXT")
    private String eligibilityCriteria;

    private Integer maxDurationMonths; // null = indefinite

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ProgramStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ProgramStatus.ACTIVE;
    }
}
