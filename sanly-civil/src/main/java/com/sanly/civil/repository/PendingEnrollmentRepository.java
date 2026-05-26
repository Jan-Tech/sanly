package com.sanly.civil.repository;

import com.sanly.civil.entity.EnrollmentStatus;
import com.sanly.civil.entity.PendingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PendingEnrollmentRepository extends JpaRepository<PendingEnrollment, UUID> {
    List<PendingEnrollment> findByExpectedSchoolYearAndStatus(Integer year, EnrollmentStatus status);
    boolean existsByChildNationalId(String childNationalId);
}
