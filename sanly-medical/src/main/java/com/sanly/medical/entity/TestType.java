package com.sanly.medical.entity;

/**
 * Medical test types performed at SANLY-registered clinics.
 * Each type maps to a SANLY Bridge DataType for inter-agency publishing.
 */
public enum TestType {
    VISION_TEST,
    HEARING_TEST,
    BLOOD_TEST,
    PHYSICAL_EXAM,
    MENTAL_HEALTH_EVAL,
    DRUG_SCREENING,
    GENERAL_CHECKUP
}
