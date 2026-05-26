package com.sanly.police.dto.response;

import com.sanly.police.entity.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class CriminalRecordResponse {
    private UUID recordId;
    private String citizenNationalId;
    private OffenseType offenseType;
    private LocalDate offenseDate;
    private Verdict verdict;
    private String sentenceDescription;
    private String courtName;
    private Long recordedByOfficerId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private RecordStatus status;
    private boolean bridgePublished;
}
