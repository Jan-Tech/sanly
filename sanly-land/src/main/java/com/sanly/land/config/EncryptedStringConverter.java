package com.sanly.land.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private final EncryptionService svc;

    @Autowired
    public EncryptedStringConverter(EncryptionService svc) { this.svc = svc; }

    @Override public String convertToDatabaseColumn(String a) { return svc.encrypt(a); }
    @Override public String convertToEntityAttribute(String d) { return svc.decrypt(d); }
}
