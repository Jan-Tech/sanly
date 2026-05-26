package com.sanly.banking.exception;

public class ConsentExpiredException extends RuntimeException {
    public ConsentExpiredException(String message) {
        super(message);
    }
}
