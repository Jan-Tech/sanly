package com.sanly.registry.entity;

import com.sanly.registry.config.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "street", columnDefinition = "TEXT")
    private String street;

    @Column(name = "city", length = 150)
    private String city;

    @Column(name = "region", length = 100)
    private String region;
}
