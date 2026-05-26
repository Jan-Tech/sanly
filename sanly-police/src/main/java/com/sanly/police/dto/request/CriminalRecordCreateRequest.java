package com.sanly.police.dto.request;

import com.sanly.police.entity.OffenseType;
import com.sanly.police.entity.Verdict;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CriminalRecordCreateRequest {
    @NotBlank @Size(min = 5, max = 30) private String citizenNationalId;
    @NotNull private OffenseType offenseType;
    @NotNull @PastOrPresent private LocalDate offenseDate;
    @NotNull private Verdict verdict;
    @Size(max = 2000) private String sentenceDescription;
    @Size(max = 200) private String courtName;
    private LocalDateTime expiresAt;
}
