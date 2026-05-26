package com.sanly.appointments.dto.request;

import com.sanly.appointments.entity.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOfficeRequest {

    @NotBlank
    @Size(max = 12)
    private String officeCode;

    @NotNull
    private InstitutionType institutionType;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String region;

    @Size(max = 500)
    private String address;

    @Size(max = 30)
    private String phone;

    private Double latitude;
    private Double longitude;
}
