package com.sanly.registry.dto.response;

import com.sanly.registry.entity.CitizenStatus;
import com.sanly.registry.entity.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CitizenResponse {
    private String nationalId;
    private String firstName;
    private String lastName;
    private String middleName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String placeOfBirth;
    private AddressResponse address;
    private String photoUrl;
    private String fatherId;
    private String motherId;
    private String spouseId;
    private CitizenStatus status;
    /** Last 4 digits of registered phone, e.g. "***7890". Null if no phone registered. */
    private String phoneMasked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
