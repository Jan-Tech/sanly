package com.sanly.court.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "case_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CaseSequence {
    @Id private Integer year;
    @Column(nullable = false) private Integer nextValue;
}
