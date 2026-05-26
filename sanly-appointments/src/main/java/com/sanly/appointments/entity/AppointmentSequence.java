package com.sanly.appointments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "appointment_sequences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "year", unique = true, nullable = false)
    private int year;

    @Column(name = "last_sequence", nullable = false)
    @Builder.Default
    private int lastSequence = 0;
}
