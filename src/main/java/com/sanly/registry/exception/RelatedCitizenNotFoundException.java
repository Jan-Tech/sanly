package com.sanly.registry.exception;

public class RelatedCitizenNotFoundException extends RuntimeException {

    public RelatedCitizenNotFoundException(String relation, String nationalId) {
        super(relation + " citizen not found with national ID: " + nationalId);
    }
}
