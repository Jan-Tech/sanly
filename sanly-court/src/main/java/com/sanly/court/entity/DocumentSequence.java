package com.sanly.court.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_sequences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentSequence {
    @Id private Integer year;
    @Column(nullable = false) private Integer nextValue;
}
