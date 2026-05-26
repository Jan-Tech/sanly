package com.sanly.tax.exception;
public class TaxpayerNotFoundException extends RuntimeException {
    public TaxpayerNotFoundException(String id) { super("Taxpayer not found: " + id); }
}
