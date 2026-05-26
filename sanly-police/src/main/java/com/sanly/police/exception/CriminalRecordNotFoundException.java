package com.sanly.police.exception;
import java.util.UUID;
public class CriminalRecordNotFoundException extends RuntimeException {
    public CriminalRecordNotFoundException(UUID id) { super("Criminal record not found: " + id); }
}
