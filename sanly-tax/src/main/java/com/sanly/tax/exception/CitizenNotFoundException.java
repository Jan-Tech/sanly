package com.sanly.tax.exception;
public class CitizenNotFoundException extends RuntimeException {
    public CitizenNotFoundException(String id) { super("Citizen not found: " + id); }
}
