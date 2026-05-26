package com.sanly.court.repository;

import com.sanly.court.entity.Court;
import com.sanly.court.entity.CourtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourtRepository extends JpaRepository<Court, UUID> {
    Optional<Court> findByCourtCode(String courtCode);
    boolean existsByCourtCode(String courtCode);
    List<Court> findByStatus(CourtStatus status);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.courtCode, 8) AS int)), 0) FROM Court c WHERE c.courtCode LIKE 'TM-CRT-%'")
    int findMaxCourtSequence();
}
