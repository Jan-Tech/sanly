package com.sanly.dmv.service;

import com.sanly.dmv.dto.request.OfficerCreateRequest;
import com.sanly.dmv.dto.request.OfficerStatusRequest;
import com.sanly.dmv.dto.response.OfficerResponse;

import java.util.List;

public interface OfficerService {
    OfficerResponse register(OfficerCreateRequest request);
    List<OfficerResponse> listAll();
    OfficerResponse getById(Long officerId);
    OfficerResponse updateStatus(Long officerId, OfficerStatusRequest request);
}
