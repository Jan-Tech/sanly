package com.sanly.vehicle.entity;

import com.sanly.vehicle.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "technical_inspections")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TechnicalInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inspectionId;

    @Column(nullable = false, length = 12)
    private String plateNumber;

    @Column(nullable = false)
    private LocalDate inspectionDate;

    @Column(nullable = false)
    private LocalDate nextInspectionDue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InspectionResult result;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 2048)
    private String findings;

    private Long inspectedByOfficerId;
}
