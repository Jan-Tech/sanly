package com.sanly.dmv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-year counter for license number generation.
 * Pessimistic-write-locked during issuance to guarantee uniqueness
 * under concurrent requests within the same calendar year.
 */
@Entity
@Table(name = "license_sequences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseSequence {

    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_counter", nullable = false)
    @Builder.Default
    private int lastCounter = 0;
}
