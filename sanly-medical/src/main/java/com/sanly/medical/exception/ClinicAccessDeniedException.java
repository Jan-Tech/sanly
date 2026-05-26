package com.sanly.medical.exception;

/** Thrown when a doctor tries to access a record belonging to a different clinic. */
public class ClinicAccessDeniedException extends RuntimeException {
    public ClinicAccessDeniedException() {
        super("Access denied — this record belongs to a different clinic");
    }
}
