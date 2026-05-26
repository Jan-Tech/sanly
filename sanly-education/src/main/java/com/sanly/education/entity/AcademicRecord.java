package com.sanly.education.entity;

import com.sanly.education.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "academic_records")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class AcademicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    @Column(nullable = false)
    private String citizenNationalId;

    @Column(nullable = false)
    private String institutionCode;

    @Column(nullable = false)
    private String academicYear; // e.g. "2023-2024"

    @Convert(converter = EncryptedStringConverter.class)
    private String grade; // encrypted

    @Convert(converter = EncryptedStringConverter.class)
    private String gpa; // encrypted

    @Convert(converter = EncryptedStringConverter.class)
    private String notes; // encrypted

    private UUID recordedByOfficerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
