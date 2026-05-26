package com.sanly.registry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewActionRequest {

    /**
     * Reviewer notes or resolution description depending on the endpoint used.
     * For approve/partial: stored as resolutionDescription.
     * For reject: stored as reviewerNotes.
     */
    @NotBlank
    private String notes;
}
