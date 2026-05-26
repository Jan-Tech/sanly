package com.sanly.registry.audit;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {

    /**
     * Logs a successful operation on a citizen record (status 200/201/204).
     *
     * @param action           action constant, e.g. "CITIZEN_READ"
     * @param citizenNationalId the NIN of the affected citizen, or null for bulk ops
     * @param request          incoming HTTP request (provides IP, method, URI)
     */
    void log(String action, String citizenNationalId, HttpServletRequest request);

    /**
     * Logs a failed/rejected operation (non-2xx response).
     *
     * @param action            action constant
     * @param citizenNationalId the NIN attempted, or null
     * @param request           incoming HTTP request
     * @param statusCode        HTTP status code returned
     * @param details           reason for failure
     */
    void logFailure(String action, String citizenNationalId,
                    HttpServletRequest request, int statusCode, String details);
}
