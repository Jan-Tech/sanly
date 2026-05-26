package com.sanly.bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Grants {@code requestingCode} permission to query data of type {@code dataType}
 * from {@code targetCode}. Soft-deleted via {@code active=false}.
 */
@Entity
@Table(name = "institution_permissions",
    indexes = {
        @Index(name = "idx_perm_requesting", columnList = "requesting_code"),
        @Index(name = "idx_perm_target",     columnList = "target_code"),
        @Index(name = "idx_perm_active",     columnList = "active")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uq_inst_permission",
        columnNames = {"requesting_code", "target_code", "data_type"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requesting_code", length = 50, nullable = false)
    private String requestingCode;

    @Column(name = "target_code", length = 50, nullable = false)
    private String targetCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 50, nullable = false)
    private DataType dataType;

    @Column(name = "granted_by", length = 200)
    private String grantedBy;

    @CreationTimestamp
    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
