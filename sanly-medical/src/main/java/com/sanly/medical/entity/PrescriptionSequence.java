package com.sanly.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-year counter for prescription codes (TM-RX-YYYYNNNNNN).
 * Acquired with PESSIMISTIC_WRITE to prevent duplicate codes under concurrent issuance.
 */
@Entity
@Table(name = "prescription_sequences")
@Data
@NoArgsConstructor
public class PrescriptionSequence {

    @Id
    @Column(name = "seq_year")
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Long nextValue = 1L;
}
