package com.sanly.pension.dto.response;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record PensionAccountResponse(
        Long accountId, String accountCode, String citizenNationalId,
        LocalDateTime openedAt, LocalDate employmentStartDate, LocalDate birthDate,
        String gender, Integer retirementAgeTarget, LocalDate eligibleAt,
        String totalContributions, String totalEmployerContributions, String totalCitizenContributions,
        String status) {}
