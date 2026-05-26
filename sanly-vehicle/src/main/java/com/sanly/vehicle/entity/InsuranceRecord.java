package com.sanly.vehicle.entity;

import com.sanly.vehicle.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "insurance_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InsuranceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long insuranceId;

    @Column(nullable = false, length = 12)
    private String plateNumber;

    @Column(nullable = false, length = 200)
    private String insuranceCompany;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CoverageType coverageType;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsuranceStatus status;

    private Long registeredByOfficerId;
}
