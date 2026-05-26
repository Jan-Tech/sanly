package com.sanly.pension.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employer_sequence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployerSequence {
    @Id private Long id;
    @Column(nullable = false) private long lastNumber;
}
