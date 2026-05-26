package com.sanly.dmv.exception;
public class VisionTestFailedException extends RuntimeException {
    public VisionTestFailedException() {
        super("Vision test result is FAIL. License cannot be issued.");
    }
}
