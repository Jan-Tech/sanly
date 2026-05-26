package com.sanly.bridge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "institutions",
    indexes = @Index(name = "idx_institutions_status", columnList = "status"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Institution {

    /** e.g. INST_MEDICAL, INST_DMV, INST_TAX */
    @Id
    @Column(name = "institution_code", length = 50, nullable = false, updatable = false)
    private String institutionCode;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** BCrypt hash of the raw API key — raw key is never stored. */
    @Column(name = "hashed_api_key", nullable = false, length = 255)
    private String hashedApiKey;

    /**
     * Data types this institution is authorized to publish.
     * Used as a guard in the publish flow — institutions may only push
     * data types they have declared.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "institution_publishable_types",
        joinColumns = @JoinColumn(name = "institution_code")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 50)
    @Builder.Default
    private Set<DataType> publishableTypes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InstitutionStatus status = InstitutionStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
