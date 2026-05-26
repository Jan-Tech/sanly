package com.sanly.registry.exception;

public class InvalidNationalIdException extends RuntimeException {

    public InvalidNationalIdException(String nationalId) {
        super("Invalid TM-NIN format: " + nationalId);
    }
}
