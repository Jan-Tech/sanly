package com.sanly.registry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_citizen_id",   columnList = "citizen_national_id"),
        @Index(name = "idx_audit_accessed_at",  columnList = "accessed_at"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_action",       columnList = "action")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "citizen_national_id", length = 11)
    private String citizenNationalId;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "performed_by", nullable = false, length = 200)
    private String performedBy;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "endpoint", length = 300)
    private String endpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;
}
