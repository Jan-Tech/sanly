package com.sanly.registry.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @Size(max = 300, message = "Street must not exceed 300 characters")
    private String street;

    @Size(max = 150, message = "City must not exceed 150 characters")
    private String city;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;
}
