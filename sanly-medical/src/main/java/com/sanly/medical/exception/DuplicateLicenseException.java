package com.sanly.medical.exception;

public class DuplicateLicenseException extends RuntimeException {
    public DuplicateLicenseException(String type, String number) {
        super(type + " with license number '" + number + "' already exists");
    }
}
