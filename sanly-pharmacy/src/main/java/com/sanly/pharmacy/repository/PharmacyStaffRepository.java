package com.sanly.pharmacy.repository;

import com.sanly.pharmacy.entity.PharmacyStaff;
import com.sanly.pharmacy.entity.StaffStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PharmacyStaffRepository extends JpaRepository<PharmacyStaff, Long> {
    Optional<PharmacyStaff> findByUsernameAndStatus(String username, StaffStatus status);
    Optional<PharmacyStaff> findByUsername(String username);
    List<PharmacyStaff> findAllByOrderByCreatedAtDesc();
    boolean existsByNationalId(String nationalId);
    boolean existsByUsername(String username);
}
