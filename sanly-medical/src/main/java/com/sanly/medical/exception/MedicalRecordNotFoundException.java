package com.sanly.medical.exception;

import java.util.UUID;

public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(UUID recordId) {
        super("Medical record not found: " + recordId);
    }
}
