package com.sanly.bridge.entity;

/**
 * Mandatory justification code for every data query through SANLY Bridge.
 * Prevents "favor culture" access — every query must have a declared purpose.
 */
public enum QueryPurpose {
    TRAFFIC_STOP,
    CRIMINAL_INVESTIGATION,
    COURT_ORDER,
    ROUTINE_CHECK,
    BORDER_CONTROL,
    EMERGENCY,
    LICENSE_ISSUANCE,
    TAX_AUDIT,
    BUSINESS_VERIFICATION,
    CIVIL_REGISTRATION,
    MEDICAL_CLEARANCE,
    OTHER
}
