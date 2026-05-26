package com.sanly.education.dto.response;

import com.sanly.education.entity.Diploma;
import com.sanly.education.entity.DiplomaStatus;
import com.sanly.education.entity.Honors;
import com.sanly.education.entity.ProgramLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DiplomaResponse(
        UUID diplomaId,
        String diplomaCode,
        String citizenNationalId,
        String institutionCode,
        String programName,
        ProgramLevel programLevel,
        LocalDate graduationDate,
        Honors honors,
        UUID issuedByOfficerId,
        LocalDateTime issuedAt,
        DiplomaStatus status,
        String revokedReason
) {
    public static DiplomaResponse from(Diploma d) {
        return new DiplomaResponse(d.getDiplomaId(), d.getDiplomaCode(), d.getCitizenNationalId(),
                d.getInstitutionCode(), d.getProgramName(), d.getProgramLevel(),
                d.getGraduationDate(), d.getHonors(), d.getIssuedByOfficerId(),
                d.getIssuedAt(), d.getStatus(), d.getRevokedReason());
    }
}
