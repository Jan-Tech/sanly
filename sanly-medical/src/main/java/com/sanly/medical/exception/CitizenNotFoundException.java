package com.sanly.medical.exception;

public class CitizenNotFoundException extends RuntimeException {
    public CitizenNotFoundException(String nationalId) {
        super("Citizen not found in SANLY Registry: " + nationalId);
    }
}
