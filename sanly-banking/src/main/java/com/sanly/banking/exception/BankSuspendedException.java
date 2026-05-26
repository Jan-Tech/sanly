package com.sanly.banking.exception;

public class BankSuspendedException extends RuntimeException {
    public BankSuspendedException(String message) {
        super(message);
    }
}
