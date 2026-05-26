package com.sanly.pension.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    private final EncryptionService encryptionService;
    @Override public String convertToDatabaseColumn(String a) { return encryptionService.encrypt(a); }
    @Override public String convertToEntityAttribute(String d) { return encryptionService.decrypt(d); }
}
