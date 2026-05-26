package com.sanly.social.entity;

import com.sanly.social.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "unemployment_records")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class UnemploymentRecord {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    @Column(nullable = false, unique = true)
    private String citizenNationalId;

    @Column(nullable = false) private LocalDateTime registeredAt;

    @Convert(converter = EncryptedStringConverter.class)
    private String lastEmployer; // encrypted

    private LocalDate lastEmploymentDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private UnemploymentReason reason;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private UnemploymentStatus status;

    private LocalDateTime updatedAt;

    @PrePersist void prePersist() {
        if (registeredAt == null) registeredAt = LocalDateTime.now();
        updatedAt = registeredAt;
        if (status == null) status = UnemploymentStatus.REGISTERED;
    }

    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
}
