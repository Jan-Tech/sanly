package com.sanly.registry.entity;

import com.sanly.registry.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "citizens",
    indexes = {
        @Index(name = "idx_citizens_last_name",  columnList = "last_name"),
        @Index(name = "idx_citizens_first_name", columnList = "first_name"),
        @Index(name = "idx_citizens_dob",        columnList = "date_of_birth"),
        @Index(name = "idx_citizens_region",     columnList = "region"),
        @Index(name = "idx_citizens_status",     columnList = "status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {

    @Id
    @Column(name = "national_id", length = 11, nullable = false, updatable = false)
    private String nationalId;

    @Column(name = "first_name", length = 150, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 150, nullable = false)
    private String lastName;

    @Column(name = "middle_name", length = 150)
    private String middleName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 6)
    private Gender gender;

    // Encrypted: sensitive PII not needed for search
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "place_of_birth", columnDefinition = "TEXT")
    private String placeOfBirth;

    @Embedded
    private Address address;

    // Encrypted: photo URL may be an internal signed URL
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    // Encrypted: E.164 format, e.g. +99361234567
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "phone_number", columnDefinition = "TEXT")
    private String phoneNumber;

    @Column(name = "father_id", length = 11)
    private String fatherId;

    @Column(name = "mother_id", length = 11)
    private String motherId;

    @Column(name = "spouse_id", length = 11)
    private String spouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private CitizenStatus status = CitizenStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
