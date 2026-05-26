package com.sanly.appointments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "service_types",
    indexes = {
        @Index(name = "idx_svc_office", columnList = "office_code")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_type_id")
    private UUID serviceTypeId;

    @Column(name = "office_code", length = 12, nullable = false)
    private String officeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "institution_type", length = 25, nullable = false)
    private InstitutionType institutionType;

    @Column(name = "service_name", length = 200, nullable = false)
    private String serviceName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private int durationMinutes = 30;

    @Column(name = "requires_documents", length = 1000)
    private String requiresDocuments;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
