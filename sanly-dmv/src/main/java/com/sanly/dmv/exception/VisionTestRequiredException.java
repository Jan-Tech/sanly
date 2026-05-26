package com.sanly.dmv.exception;
public class VisionTestRequiredException extends RuntimeException {
    public VisionTestRequiredException() {
        super("No valid vision test found in SANLY registry. " +
              "Citizen must visit a registered clinic first.");
    }
}
