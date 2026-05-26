package com.sanly.social.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "claim_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ClaimSequence {
    @Id private Integer year;
    @Column(nullable = false) private Long nextValue;
}
