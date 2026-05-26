package com.sanly.tax.exception;
public class DuplicateTaxpayerException extends RuntimeException {
    public DuplicateTaxpayerException(String nationalId) {
        super("Citizen already registered as taxpayer: " + nationalId);
    }
}
