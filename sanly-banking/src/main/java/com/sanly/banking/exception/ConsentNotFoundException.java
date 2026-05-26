package com.sanly.banking.exception;

public class ConsentNotFoundException extends RuntimeException {
    public ConsentNotFoundException(String message) {
        super(message);
    }
}
