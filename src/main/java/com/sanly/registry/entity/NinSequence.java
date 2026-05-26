package com.sanly.registry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tracks the last-used sequence number per TM-NIN prefix (7-char string:
 * gender/century digit + YYMMDD). Pessimistic-write-locked by the service
 * during registration to guarantee uniqueness without gaps under concurrency.
 */
@Entity
@Table(name = "nin_sequences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NinSequence {

    @Id
    @Column(name = "prefix", length = 7, nullable = false)
    private String prefix;

    @Column(name = "last_sequence", nullable = false)
    @Builder.Default
    private int lastSequence = 0;
}
