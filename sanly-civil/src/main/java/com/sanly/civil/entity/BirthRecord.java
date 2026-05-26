package com.sanly.civil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "birth_records")
@Getter
@Setter
public class BirthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String certificateNumber;

    @Column(nullable = false, unique = true)
    private String childNationalId;

    @Column(nullable = false)
    private String childFirstName;

    @Column(nullable = false)
    private String childLastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String placeOfBirth;

    private String fatherNationalId;
    private String fatherFullName;
    private String motherNationalId;
    private String motherFullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registering_officer_id")
    private CivilOfficer registeringOfficer;

    @Column(nullable = false)
    private boolean bridgePublished;

    @CreationTimestamp
    private Instant createdAt;
}
