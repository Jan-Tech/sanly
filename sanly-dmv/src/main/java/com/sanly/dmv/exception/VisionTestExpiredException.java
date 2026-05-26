package com.sanly.dmv.exception;
public class VisionTestExpiredException extends RuntimeException {
    public VisionTestExpiredException(String expiryDate) {
        super("Vision test expired on " + expiryDate +
              ". Citizen must renew at a registered clinic.");
    }
}
