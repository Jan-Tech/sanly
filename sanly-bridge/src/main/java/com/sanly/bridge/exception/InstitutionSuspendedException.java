package com.sanly.bridge.exception;

public class InstitutionSuspendedException extends RuntimeException {
    public InstitutionSuspendedException(String code) {
        super("Institution is suspended: " + code);
    }
}
