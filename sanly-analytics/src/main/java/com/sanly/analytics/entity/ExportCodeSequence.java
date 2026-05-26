package com.sanly.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "export_code_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExportCodeSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private int year;

    @Column(name = "next_val", nullable = false)
    private long nextVal;
}
