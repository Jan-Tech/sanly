package com.sanly.civil.entity;

import com.sanly.civil.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "death_records")
@Getter
@Setter
public class DeathRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String certificateNumber;

    @Column(nullable = false, unique = true)
    private String deceasedNationalId;

    @Column(nullable = false)
    private String deceasedFullName;

    @Column(nullable = false)
    private LocalDate dateOfDeath;

    @Column(nullable = false)
    private String placeOfDeath;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String deathCause;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registering_officer_id")
    private CivilOfficer registeringOfficer;

    @Column(nullable = false)
    private boolean bridgePublished;

    @CreationTimestamp
    private Instant createdAt;
}
