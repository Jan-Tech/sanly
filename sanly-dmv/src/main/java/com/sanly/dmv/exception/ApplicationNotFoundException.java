package com.sanly.dmv.exception;
import java.util.UUID;
public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(UUID id) { super("Application not found: " + id); }
}
