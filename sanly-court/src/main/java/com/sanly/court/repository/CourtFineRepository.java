package com.sanly.court.repository;

import com.sanly.court.entity.CourtFine;
import com.sanly.court.entity.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourtFineRepository extends JpaRepository<CourtFine, UUID> {
    Optional<CourtFine> findByFineCode(String fineCode);
    List<CourtFine> findByCitizenNationalId(String nationalId);
    List<CourtFine> findByCaseNumber(String caseNumber);
    List<CourtFine> findByStatus(FineStatus status);

    // For overdue job: OUTSTANDING fines past dueDate
    @Query("SELECT f FROM CourtFine f WHERE f.status = 'OUTSTANDING' AND f.dueDate < :today")
    List<CourtFine> findOutstandingOverdue(LocalDate today);
}
