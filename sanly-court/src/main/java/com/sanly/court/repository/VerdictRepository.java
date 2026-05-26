package com.sanly.court.repository;

import com.sanly.court.entity.Verdict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerdictRepository extends JpaRepository<Verdict, UUID> {
    Optional<Verdict> findByCaseNumber(String caseNumber);
    boolean existsByCaseNumber(String caseNumber);
}
