package com.sanly.pharmacy.dto.request;

import com.sanly.pharmacy.entity.StaffRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStaffRequest {
    @NotBlank @Size(min = 11, max = 11) private String nationalId;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String username;
    @NotBlank @Size(min = 8) private String password;
    @NotNull private StaffRole role;
}
