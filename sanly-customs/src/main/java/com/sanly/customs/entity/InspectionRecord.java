package com.sanly.customs.entity;

import com.sanly.customs.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inspection_records")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class InspectionRecord {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID inspectionId;

    @Column(nullable = false) private String declarationCode;
    private UUID inspectorOfficerId;
    private LocalDate inspectionDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private InspectionType inspectionType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String findings;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private InspectionResult result;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (inspectionDate == null) inspectionDate = LocalDate.now();
    }
}
