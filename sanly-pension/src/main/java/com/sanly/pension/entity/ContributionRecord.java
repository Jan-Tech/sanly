package com.sanly.pension.entity;

import com.sanly.pension.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contribution_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContributionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contributionId;

    @Column(nullable = false, length = 25)
    private String accountCode;

    @Column(length = 20)
    private String employerCode;

    @Column(nullable = false, length = 7)
    private String contributionMonth;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String employerAmount;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String citizenAmount;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, length = 512)
    private String totalAmount;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private Long submittedByOfficerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContributionStatus status;

    @Column(length = 500)
    private String rejectionReason;
}
