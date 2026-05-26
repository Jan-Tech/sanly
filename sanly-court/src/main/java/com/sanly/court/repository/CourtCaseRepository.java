package com.sanly.court.repository;

import com.sanly.court.entity.CaseStatus;
import com.sanly.court.entity.CourtCase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourtCaseRepository extends JpaRepository<CourtCase, UUID> {
    Optional<CourtCase> findByCaseNumber(String caseNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CourtCase c WHERE c.caseNumber = :caseNumber")
    Optional<CourtCase> findByCaseNumberWithLock(String caseNumber);

    List<CourtCase> findByPlaintiffNationalId(String nationalId);
    List<CourtCase> findByDefendantNationalId(String nationalId);
    List<CourtCase> findByCourtCode(String courtCode);
    List<CourtCase> findByCourtCodeAndStatus(String courtCode, CaseStatus status);

    // Cases with hearing date within the next N days (for reminder)
    @Query("SELECT c FROM CourtCase c WHERE c.hearingDate IS NOT NULL AND c.hearingDate <= :cutoff AND c.status NOT IN ('DECIDED','CLOSED','DISMISSED')")
    List<CourtCase> findUpcomingHearings(LocalDate cutoff);
}
