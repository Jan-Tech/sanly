package com.sanly.medical.exception;

public class ClinicSuspendedException extends RuntimeException {
    public ClinicSuspendedException(Long clinicId) {
        super("Clinic " + clinicId + " is suspended and cannot accept new records");
    }
}
