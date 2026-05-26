package com.sanly.dmv.exception;
public class OfficerNotFoundException extends RuntimeException {
    public OfficerNotFoundException(Long id) { super("Officer not found with id: " + id); }
}
