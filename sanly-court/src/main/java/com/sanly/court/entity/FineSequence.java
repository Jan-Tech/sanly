package com.sanly.court.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fine_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FineSequence {
    @Id private Integer year;
    @Column(nullable = false) private Integer nextValue;
}
