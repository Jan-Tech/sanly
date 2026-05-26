package com.sanly.registry.exception;

public class CitizenNotFoundException extends RuntimeException {

    public CitizenNotFoundException(String nationalId) {
        super("Citizen not found with national ID: " + nationalId);
    }
}
