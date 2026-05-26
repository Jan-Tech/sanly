package com.sanly.vehicle.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plate_sequence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlateSequence {

    @Id
    private Long id;

    @Column(nullable = false)
    private long lastNumber;
}
