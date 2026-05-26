package com.sanly.police.exception;
public class OfficerNotFoundException extends RuntimeException {
    public OfficerNotFoundException(Long id) { super("Officer not found: " + id); }
}
