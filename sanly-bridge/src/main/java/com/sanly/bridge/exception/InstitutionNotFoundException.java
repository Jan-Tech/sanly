package com.sanly.bridge.exception;

public class InstitutionNotFoundException extends RuntimeException {
    public InstitutionNotFoundException(String code) {
        super("Institution not found: " + code);
    }
}
