package com.sanly.court.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService svc) {
        EncryptedStringConverter.encryptionService = svc;
    }

    @Override public String convertToDatabaseColumn(String attribute) { return encryptionService.encrypt(attribute); }
    @Override public String convertToEntityAttribute(String dbData) { return encryptionService.decrypt(dbData); }
}
