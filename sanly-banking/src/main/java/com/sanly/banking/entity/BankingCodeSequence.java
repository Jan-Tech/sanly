package com.sanly.banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "banking_code_sequences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankingCodeSequence {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "sequence_type", unique = true, nullable = false)
    private String sequenceType;

    @Column(name = "year")
    private int year;

    @Column(name = "next_val")
    private long nextVal;
}
