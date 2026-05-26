package com.sanly.tax.exception;
import java.util.UUID;
public class TaxFilingNotFoundException extends RuntimeException {
    public TaxFilingNotFoundException(UUID id) { super("Tax filing not found: " + id); }
}
