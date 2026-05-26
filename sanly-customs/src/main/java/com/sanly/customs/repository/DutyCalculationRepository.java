package com.sanly.customs.repository;

import com.sanly.customs.entity.DutyCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DutyCalculationRepository extends JpaRepository<DutyCalculation, UUID> {
    Optional<DutyCalculation> findByDeclarationCode(String declarationCode);
    boolean existsByDeclarationCode(String declarationCode);
}
