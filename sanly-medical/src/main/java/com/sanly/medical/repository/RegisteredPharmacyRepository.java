package com.sanly.medical.repository;

import com.sanly.medical.entity.PharmacyStatus;
import com.sanly.medical.entity.RegisteredPharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegisteredPharmacyRepository extends JpaRepository<RegisteredPharmacy, Long> {
    Optional<RegisteredPharmacy> findByPharmacyCode(String pharmacyCode);
    Optional<RegisteredPharmacy> findByPharmacyCodeAndStatus(String pharmacyCode, PharmacyStatus status);
    List<RegisteredPharmacy> findAllByOrderByRegisteredAtDesc();
    boolean existsByPharmacyCode(String pharmacyCode);
    boolean existsByLicenseNumber(String licenseNumber);
}
