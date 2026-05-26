package com.sanly.vehicle.entity;

import com.sanly.vehicle.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column(nullable = false, length = 12)
    private String plateNumber;

    @Column(nullable = false, length = 20)
    private String fromNationalId;

    @Column(length = 20)
    private String toNationalId;

    @Column(length = 20)
    private String toBusinessNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferType transferType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String agreedPrice;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    private Long processedByOfficerId;

    private LocalDateTime processedAt;

    @Column(length = 500)
    private String rejectionReason;
}
