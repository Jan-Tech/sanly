package com.sanly.business.repository;

import com.sanly.business.entity.ApplicationStatus;
import com.sanly.business.entity.RegistrationApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistrationApplicationRepository extends JpaRepository<RegistrationApplication, UUID> {
    Page<RegistrationApplication> findByStatus(ApplicationStatus status, Pageable pageable);
    Page<RegistrationApplication> findByApplicantNationalId(String applicantNationalId, Pageable pageable);
}
