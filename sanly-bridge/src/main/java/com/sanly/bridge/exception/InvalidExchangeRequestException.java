package com.sanly.bridge.exception;

public class InvalidExchangeRequestException extends RuntimeException {
    public InvalidExchangeRequestException(String message) {
        super(message);
    }
}
