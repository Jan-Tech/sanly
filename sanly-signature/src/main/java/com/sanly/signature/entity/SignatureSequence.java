package com.sanly.signature.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "signature_sequences")
@Getter
@Setter
@NoArgsConstructor
public class SignatureSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int year;

    @Column(nullable = false)
    private int lastSequence = 0;
}
