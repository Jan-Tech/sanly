package com.sanly.bridge.service;

import com.sanly.bridge.dto.request.InstitutionCreateRequest;
import com.sanly.bridge.dto.request.InstitutionStatusRequest;
import com.sanly.bridge.dto.response.InstitutionKeyResponse;
import com.sanly.bridge.dto.response.InstitutionResponse;

import java.util.List;

public interface InstitutionService {

    /** Register a new institution and return its generated API key (shown once). */
    InstitutionKeyResponse register(InstitutionCreateRequest request);

    List<InstitutionResponse> listAll();

    InstitutionResponse getByCode(String institutionCode);

    InstitutionResponse updateStatus(String institutionCode, InstitutionStatusRequest request);

    /** Rotate the API key — invalidates the old key immediately. Returns new raw key. */
    InstitutionKeyResponse rotateKey(String institutionCode);
}
