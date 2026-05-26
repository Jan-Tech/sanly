package com.sanly.registry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ddr_sequences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DdrSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int year;

    @Column(nullable = false)
    @Builder.Default
    private int lastSequence = 0;
}
