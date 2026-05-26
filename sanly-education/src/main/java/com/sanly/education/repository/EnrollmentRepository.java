package com.sanly.education.repository;

import com.sanly.education.entity.Enrollment;
import com.sanly.education.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findByCitizenNationalId(String citizenNationalId);
    List<Enrollment> findByInstitutionCode(String institutionCode);
    Optional<Enrollment> findByCitizenNationalIdAndInstitutionCodeAndStatus(
            String citizenNationalId, String institutionCode, EnrollmentStatus status);
    boolean existsByCitizenNationalIdAndInstitutionCodeAndStatus(
            String citizenNationalId, String institutionCode, EnrollmentStatus status);
    List<Enrollment> findByStatusAndInstitutionCode(EnrollmentStatus status, String institutionCode);
    List<Enrollment> findByStatus(EnrollmentStatus status);
}
