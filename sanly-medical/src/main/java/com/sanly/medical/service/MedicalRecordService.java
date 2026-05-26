package com.sanly.medical.service;

import com.sanly.medical.dto.request.MedicalRecordCreateRequest;
import com.sanly.medical.dto.request.RecordResultUpdateRequest;
import com.sanly.medical.dto.response.MedicalRecordResponse;
import com.sanly.medical.dto.response.PageResponse;

import java.util.UUID;

public interface MedicalRecordService {

    /**
     * Submit a new medical test result.
     * Verifies the citizen in citizen-registry before saving.
     * Publishes to SANLY Bridge asynchronously after local save.
     */
    MedicalRecordResponse create(MedicalRecordCreateRequest request);

    /**
     * Get a specific record.
     * Doctors see only records from their own clinic.
     * Admins see all.
     */
    MedicalRecordResponse getById(UUID recordId);

    /**
     * All records for a citizen.
     * Doctors see only records from their own clinic.
     * Admins see all.
     */
    PageResponse<MedicalRecordResponse> getByNationalId(String nationalId, int page, int size);

    /**
     * Update the result of an existing record (e.g. PENDING → PASS).
     * Republishes to SANLY Bridge after update.
     * Doctors can only update records within their own clinic.
     */
    MedicalRecordResponse updateResult(UUID recordId, RecordResultUpdateRequest request);
}
