package com.sanly.pension.repository;

import com.sanly.pension.entity.ApplicationStatus;
import com.sanly.pension.entity.RetirementApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RetirementApplicationRepository extends JpaRepository<RetirementApplication, Long> {
    List<RetirementApplication> findByCitizenNationalIdOrderByAppliedAtDesc(String citizenNationalId);
    List<RetirementApplication> findByStatus(ApplicationStatus status);
    boolean existsByAccountCodeAndStatus(String accountCode, ApplicationStatus status);
}
