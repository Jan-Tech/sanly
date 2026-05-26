package com.sanly.medical.exception;

public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(Long doctorId) {
        super("Doctor not found with id: " + doctorId);
    }
}
