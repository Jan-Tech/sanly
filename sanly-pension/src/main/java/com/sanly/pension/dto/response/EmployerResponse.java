package com.sanly.pension.dto.response;
import java.time.LocalDateTime;
public record EmployerResponse(
        Long employerId, String employerCode, String businessName,
        String businessRegistrationNumber, String contactNationalId,
        LocalDateTime registeredAt, String status) {}
