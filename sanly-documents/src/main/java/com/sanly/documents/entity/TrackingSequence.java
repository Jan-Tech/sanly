package com.sanly.documents.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tracking_sequences")
@Getter
@Setter
public class TrackingSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "year", unique = true, nullable = false)
    private int year;

    @Column(name = "last_sequence", nullable = false)
    private int lastSequence = 0;
}
