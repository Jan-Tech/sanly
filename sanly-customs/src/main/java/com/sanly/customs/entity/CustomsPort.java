package com.sanly.customs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customs_ports")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class CustomsPort {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID portId;

    @Column(nullable = false, unique = true)
    private String portCode; // TM-PORT-NNN

    @Column(nullable = false) private String name;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PortType portType;

    private String region;
    private String address;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PortStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = PortStatus.ACTIVE;
    }
}
