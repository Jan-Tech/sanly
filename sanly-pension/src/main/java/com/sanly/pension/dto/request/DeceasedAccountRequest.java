package com.sanly.pension.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class DeceasedAccountRequest {
    @NotBlank private String deceasedNationalId;
}
