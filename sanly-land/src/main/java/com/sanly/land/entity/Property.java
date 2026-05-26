package com.sanly.land.entity;

import com.sanly.land.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID propertyId;

    @Column(nullable = false, unique = true)
    private String cadastralNumber; // TM-CAD-YYYYNNNNNN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String region;

    @Column(precision = 12, scale = 2)
    private BigDecimal area; // square metres

    @Convert(converter = EncryptedStringConverter.class)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    void prePersist() {
        registeredAt = LocalDateTime.now();
        if (status == null) status = PropertyStatus.REGISTERED;
    }
}
