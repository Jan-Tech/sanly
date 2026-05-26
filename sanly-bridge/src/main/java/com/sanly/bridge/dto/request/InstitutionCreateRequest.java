package com.sanly.bridge.dto.request;

import com.sanly.bridge.entity.DataType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class InstitutionCreateRequest {

    @NotBlank(message = "Institution code is required")
    @Size(min = 3, max = 50, message = "Institution code must be 3–50 characters")
    @Pattern(regexp = "^INST_[A-Z0-9_]+$",
             message = "Institution code must match INST_<UPPERCASE> (e.g. INST_MEDICAL)")
    private String institutionCode;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 200, message = "Name must be 3–200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /** Data types this institution is authorized to publish into SANLY Bridge. */
    private Set<DataType> publishableTypes;
}
