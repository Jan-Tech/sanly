package com.sanly.registry.exception;

public class RegistrationLimitExceededException extends RuntimeException {

    public RegistrationLimitExceededException(String message) {
        super(message);
    }
}
