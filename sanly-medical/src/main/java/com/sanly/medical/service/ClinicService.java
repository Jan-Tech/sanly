package com.sanly.medical.service;

import com.sanly.medical.dto.request.ClinicCreateRequest;
import com.sanly.medical.dto.request.ClinicStatusRequest;
import com.sanly.medical.dto.response.ClinicResponse;

import java.util.List;

public interface ClinicService {
    ClinicResponse register(ClinicCreateRequest request);
    List<ClinicResponse> listAll();
    ClinicResponse getById(Long clinicId);
    ClinicResponse updateStatus(Long clinicId, ClinicStatusRequest request);
}
