package com.sanly.land.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PropertySequence {

    @Id
    private Integer year;

    @Column(nullable = false)
    private Long nextValue;
}
