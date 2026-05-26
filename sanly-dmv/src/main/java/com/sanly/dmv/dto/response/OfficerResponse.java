package com.sanly.dmv.dto.response;

import com.sanly.dmv.entity.OfficerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder
public class OfficerResponse {
    private Long officerId;
    private String nationalId;
    private String firstName;
    private String lastName;
    private String officeRegion;
    private String username;
    private Set<String> roles;
    private OfficerStatus status;
    private LocalDateTime createdAt;
}
