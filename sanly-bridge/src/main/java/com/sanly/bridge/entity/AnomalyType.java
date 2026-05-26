package com.sanly.bridge.entity;

public enum AnomalyType {
    /** More than 20 queries by one institution in 1 hour. */
    HIGH_VOLUME_QUERIES,

    /** Same institution queried same citizen more than 3 times in 7 days. */
    REPEATED_CITIZEN_QUERY,

    /** CRIMINAL_RECORD or MEDICAL_CLEARANCE queried between 22:00–06:00. */
    OFF_HOURS_SENSITIVE_ACCESS,

    /** ROUTINE_CHECK purpose with no caseReference on sensitive data — possible favor access. */
    SELF_QUERY_SUSPICION,

    /** More than 50 distinct citizens queried by one institution in 1 hour. */
    BULK_CITIZEN_SCAN,

    /** Same institution received more than 5 DENIED responses in 1 hour — permission probing. */
    DENIED_REPEATED_ATTEMPT
}
