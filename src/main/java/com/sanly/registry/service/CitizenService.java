package com.sanly.registry.service;

import com.sanly.registry.dto.request.CitizenCreateRequest;
import com.sanly.registry.dto.request.CitizenUpdateRequest;
import com.sanly.registry.dto.request.StatusUpdateRequest;
import com.sanly.registry.dto.response.CitizenResponse;
import com.sanly.registry.dto.response.CitizenVerifyResponse;
import com.sanly.registry.dto.response.PageResponse;

import java.time.LocalDate;

public interface CitizenService {

    /**
     * Registers a new citizen. Auto-generates and returns a TM-NIN.
     */
    CitizenResponse register(CitizenCreateRequest request);

    /**
     * Returns the full citizen record for the given national ID.
     *
     * @throws com.sanly.registry.exception.CitizenNotFoundException if not found
     */
    CitizenResponse getByNationalId(String nationalId);

    /**
     * Returns existence and ACTIVE status for the given national ID.
     * Never throws for not-found — returns {@code exists=false} instead.
     */
    CitizenVerifyResponse verify(String nationalId);

    /**
     * Updates mutable fields of a citizen record.
     *
     * @throws com.sanly.registry.exception.CitizenNotFoundException if not found
     */
    CitizenResponse update(String nationalId, CitizenUpdateRequest request);

    /**
     * Changes the status (ACTIVE / DECEASED / SUSPENDED) of a citizen.
     *
     * @throws com.sanly.registry.exception.CitizenNotFoundException if not found
     */
    CitizenResponse updateStatus(String nationalId, StatusUpdateRequest request);

    /**
     * Searches citizens by optional name, date-of-birth, and region filters.
     * All parameters are optional; omitting all returns all citizens (paginated).
     */
    PageResponse<CitizenResponse> search(String name, LocalDate dob, String region,
                                         int page, int size);

    /** Updates the citizen's registered phone number (E.164 format). */
    CitizenResponse updatePhone(String nationalId, String phoneNumber);
}
