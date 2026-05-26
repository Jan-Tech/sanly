package com.sanly.medical.exception;

public class ClinicNotFoundException extends RuntimeException {
    public ClinicNotFoundException(Long clinicId) {
        super("Clinic not found with id: " + clinicId);
    }
}
