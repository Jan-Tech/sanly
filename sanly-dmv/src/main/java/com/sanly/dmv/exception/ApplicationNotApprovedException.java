package com.sanly.dmv.exception;
public class ApplicationNotApprovedException extends RuntimeException {
    public ApplicationNotApprovedException(String msg) { super(msg); }
}
