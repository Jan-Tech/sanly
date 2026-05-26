package com.sanly.customs.entity;

import com.sanly.customs.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cargo_items")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class CargoItem {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itemId;

    @Column(nullable = false) private String declarationCode;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String itemDescription;

    private String hsCode;
    private String quantity;

    @Enumerated(EnumType.STRING)
    private CargoUnit unit;

    @Convert(converter = EncryptedStringConverter.class)
    private String unitValue;

    @Convert(converter = EncryptedStringConverter.class)
    private String totalValue;

    private String countryOfOrigin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
