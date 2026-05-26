package com.sanly.bridge.exception;

import com.sanly.bridge.entity.DataType;

public class PermissionDeniedException extends RuntimeException {

    public PermissionDeniedException(String requestingCode, DataType dataType) {
        super(requestingCode + " does not have permission to access " + dataType + " data");
    }

    public PermissionDeniedException(String message) {
        super(message);
    }
}
