package com.sanly.pension.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employerId;

    @Column(nullable = false, unique = true, length = 20)
    private String employerCode;

    @Column(nullable = false, length = 300)
    private String businessName;

    @Column(nullable = false, unique = true, length = 50)
    private String businessRegistrationNumber;

    @Column(nullable = false, length = 20)
    private String contactNationalId;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployerStatus status;
}
