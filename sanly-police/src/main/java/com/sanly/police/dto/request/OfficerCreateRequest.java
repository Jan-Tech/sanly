package com.sanly.police.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OfficerCreateRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private String badgeNumber;
    private String rank;
    private String stationRegion;
    @NotBlank private String username;
    @NotBlank @Size(min = 8) private String password;
}
