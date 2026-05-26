package com.sanly.police.entity;

import com.sanly.police.config.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "citizen_checks",
    indexes = {
        @Index(name = "idx_cc_citizen",   columnList = "citizen_national_id"),
        @Index(name = "idx_cc_performed", columnList = "performed_at")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CitizenCheck {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID checkId;

    @Column(name = "citizen_national_id", length = 30, nullable = false)
    private String citizenNationalId;

    @Column(name = "checked_by_officer_id", nullable = false)
    private Long checkedByOfficerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_type", length = 20, nullable = false)
    private CheckType checkType;

    /** Encrypted JSON: {"DRIVING_LICENSE":[...], "TAX_STATUS":[...]} */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bridge_results", columnDefinition = "TEXT")
    private String bridgeResults;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;
}
