package com.sanly.police.exception;
import java.util.UUID;
public class CitizenCheckNotFoundException extends RuntimeException {
    public CitizenCheckNotFoundException(UUID id) { super("Citizen check not found: " + id); }
}
