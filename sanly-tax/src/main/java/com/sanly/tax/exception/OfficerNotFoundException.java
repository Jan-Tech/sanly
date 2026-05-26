package com.sanly.tax.exception;
public class OfficerNotFoundException extends RuntimeException {
    public OfficerNotFoundException(Long id) { super("Officer not found: " + id); }
}
