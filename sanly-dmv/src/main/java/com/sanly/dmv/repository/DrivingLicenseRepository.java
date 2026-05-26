package com.sanly.dmv.repository;

import com.sanly.dmv.entity.DrivingLicense;
import com.sanly.dmv.entity.LicenseCategory;
import com.sanly.dmv.entity.LicenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DrivingLicenseRepository extends JpaRepository<DrivingLicense, UUID> {

    Page<DrivingLicense> findByCitizenNationalIdOrderByIssuedAtDesc(
            String citizenNationalId, Pageable pageable);

    Optional<DrivingLicense> findByLicenseNumber(String licenseNumber);

    boolean existsByCitizenNationalIdAndCategoryAndStatus(
            String citizenNationalId, LicenseCategory category, LicenseStatus status);
}
