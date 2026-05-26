package com.sanly.bridge.entity;

/**
 * Extensible set of inter-agency data types in the SANLY platform.
 * New types are added here and the DB stores the enum name as a VARCHAR.
 */
public enum DataType {
    // ── Medical ──────────────────────────────────────────────────────────────
    VISION_TEST,
    MEDICAL_CLEARANCE,

    // ── Police ───────────────────────────────────────────────────────────────
    CRIMINAL_RECORD,

    // ── Tax ──────────────────────────────────────────────────────────────────
    TAX_STATUS,

    // ── Education / misc ─────────────────────────────────────────────────────
    EDUCATION_DIPLOMA,

    // ── DMV ──────────────────────────────────────────────────────────────────
    DRIVING_LICENSE,

    // ── Property / welfare ───────────────────────────────────────────────────
    PROPERTY_RECORD,
    SOCIAL_BENEFIT_STATUS,

    // ── Business Registry ─────────────────────────────────────────────────── Step 5
    BUSINESS_REGISTRATION,

    // ── Civil Registry ────────────────────────────────────────────────────── Step 5
    BIRTH_RECORD,
    MARRIAGE_RECORD,
    DEATH_RECORD,

    // ── Medical — Prescription ─────────────────────────────────────────────
    PRESCRIPTION,

    // ── Customs ───────────────────────────────────────────────────────────────
    CUSTOMS_CLEARANCE,

    // ── Court ─────────────────────────────────────────────────────────────────
    COURT_ORDER,

    // ── Vehicle Registry ──────────────────────────────────────────────────────
    VEHICLE_RECORD,

    // ── Pension ───────────────────────────────────────────────────────────────
    PENSION_STATUS,

    // ── Digital Signature ─────────────────────────────────────────────────────
    DIGITAL_SIGNATURE
}
