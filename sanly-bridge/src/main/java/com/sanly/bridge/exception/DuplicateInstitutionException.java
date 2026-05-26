package com.sanly.bridge.exception;

public class DuplicateInstitutionException extends RuntimeException {
    public DuplicateInstitutionException(String code) {
        super("Institution already registered: " + code);
    }
}
