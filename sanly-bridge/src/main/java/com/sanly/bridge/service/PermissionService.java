package com.sanly.bridge.service;

import com.sanly.bridge.dto.request.PermissionCreateRequest;
import com.sanly.bridge.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse grant(PermissionCreateRequest request, String grantedBy);

    List<PermissionResponse> listAll();

    /** Soft-revoke: sets active=false. The record is kept for audit. */
    void revoke(Long permissionId);
}
