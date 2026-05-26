package com.sanly.pension.entity;

import com.sanly.pension.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pension_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PensionAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(nullable = false, unique = true, length = 25)
    private String accountCode;

    @Column(nullable = false, unique = true, length = 20)
    private String citizenNationalId;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Column(nullable = false)
    private LocalDate employmentStartDate;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 1)
    private String gender;

    @Column(nullable = false)
    private Integer retirementAgeTarget;

    @Column(nullable = false)
    private LocalDate eligibleAt;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String totalContributions;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String totalEmployerContributions;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String totalCitizenContributions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;
}
