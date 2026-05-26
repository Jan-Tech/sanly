package com.sanly.customs.entity;

import com.sanly.customs.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "duty_calculations")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class DutyCalculation {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID calculationId;

    @Column(nullable = false, unique = true)
    private String declarationCode;

    private String hsCode;
    private BigDecimal dutyRatePercent;
    private BigDecimal vatRatePercent;

    @Convert(converter = EncryptedStringConverter.class)
    private String calculatedDutyAmount;

    @Convert(converter = EncryptedStringConverter.class)
    private String vatAmount;

    @Convert(converter = EncryptedStringConverter.class)
    private String totalOwed;

    private String dutyRateOverride;      // officer override justification
    private UUID calculatedByOfficerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime calculatedAt;

    @PrePersist void prePersist() { calculatedAt = LocalDateTime.now(); }
}
