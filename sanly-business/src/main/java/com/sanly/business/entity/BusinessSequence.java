package com.sanly.business.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_sequences")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BusinessSequence {
    @Id private Integer year;
    @Column(name = "last_counter", nullable = false) @Builder.Default private int lastCounter = 0;
}
