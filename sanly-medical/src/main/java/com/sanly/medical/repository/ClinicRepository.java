package com.sanly.medical.repository;

import com.sanly.medical.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    Optional<Clinic> findByClinicId(Long clinicId);
}
