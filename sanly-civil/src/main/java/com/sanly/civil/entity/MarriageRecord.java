package com.sanly.civil.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "marriage_records")
@Getter
@Setter
public class MarriageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String certificateNumber;

    @Column(nullable = false)
    private String spouse1NationalId;

    @Column(nullable = false)
    private String spouse1FullName;

    @Column(nullable = false)
    private String spouse2NationalId;

    @Column(nullable = false)
    private String spouse2FullName;

    @Column(nullable = false)
    private LocalDate marriageDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarriageStatus status;

    private LocalDate dissolutionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registering_officer_id")
    private CivilOfficer registeringOfficer;

    @Column(nullable = false)
    private boolean bridgePublished;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
