package com.sanly.customs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "declaration_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DeclarationSequence {
    @Id private Integer year;
    @Column(nullable = false) private Integer nextValue;
}
