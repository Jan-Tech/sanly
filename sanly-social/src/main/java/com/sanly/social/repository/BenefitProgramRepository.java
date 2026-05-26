package com.sanly.social.repository;

import com.sanly.social.entity.BenefitProgram;
import com.sanly.social.entity.BenefitType;
import com.sanly.social.entity.ProgramStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BenefitProgramRepository extends JpaRepository<BenefitProgram, UUID> {
    Optional<BenefitProgram> findByProgramCode(String programCode);
    boolean existsByProgramCode(String programCode);
    List<BenefitProgram> findByStatus(ProgramStatus status);
    Optional<BenefitProgram> findFirstByBenefitTypeAndStatus(BenefitType type, ProgramStatus status);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.programCode, 8) AS int)), 0) FROM BenefitProgram p WHERE p.programCode LIKE 'TM-BEN-%'")
    int findMaxProgramSequence();
}
