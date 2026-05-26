package com.sanly.pension.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_sequence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountSequence {
    @Id private Long id;
    @Column(nullable = false) private long lastNumber;
}
