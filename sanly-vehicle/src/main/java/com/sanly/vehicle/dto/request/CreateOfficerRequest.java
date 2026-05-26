package com.sanly.vehicle.dto.request;

import com.sanly.vehicle.entity.OfficerRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOfficerRequest {
    @NotBlank private String nationalId;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String username;
    @NotBlank private String password;
    private String region;
    @NotNull private OfficerRole role;
}
