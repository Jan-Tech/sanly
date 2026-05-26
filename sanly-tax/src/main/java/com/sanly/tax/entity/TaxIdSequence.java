package com.sanly.tax.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tax_id_sequences")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxIdSequence {

    @Id
    private Integer year;

    @Column(name = "last_counter", nullable = false)
    @Builder.Default
    private int lastCounter = 0;
}
