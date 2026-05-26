package com.sanly.medical.repository;

import com.sanly.medical.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByNationalId(String nationalId);

    List<Doctor> findByClinicId(Long clinicId);
}
