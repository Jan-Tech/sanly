package com.sanly.medical.service;

import com.sanly.medical.dto.request.DoctorCreateRequest;
import com.sanly.medical.dto.request.DoctorStatusRequest;
import com.sanly.medical.dto.response.DoctorResponse;

import java.util.List;

public interface DoctorService {
    DoctorResponse register(DoctorCreateRequest request);
    List<DoctorResponse> listAll();
    DoctorResponse getById(Long doctorId);
    DoctorResponse updateStatus(Long doctorId, DoctorStatusRequest request);
}
