package com.sanly.police.exception;
public class CitizenNotFoundException extends RuntimeException {
    public CitizenNotFoundException(String id) { super("Citizen not found: " + id); }
}
