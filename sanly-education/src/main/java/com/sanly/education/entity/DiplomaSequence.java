package com.sanly.education.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "diploma_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DiplomaSequence {

    @Id
    private Integer year;

    @Column(nullable = false)
    private Long nextValue;
}
