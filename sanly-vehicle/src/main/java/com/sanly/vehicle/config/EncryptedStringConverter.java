package com.sanly.vehicle.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private final EncryptionService encryptionService;

    @Override public String convertToDatabaseColumn(String attribute) { return encryptionService.encrypt(attribute); }
    @Override public String convertToEntityAttribute(String dbData) { return encryptionService.decrypt(dbData); }
}
