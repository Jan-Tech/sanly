package com.sanly.notifications.exception;

public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String eventType, String language) {
        super("No active template for event=" + eventType + " language=" + language);
    }
}
