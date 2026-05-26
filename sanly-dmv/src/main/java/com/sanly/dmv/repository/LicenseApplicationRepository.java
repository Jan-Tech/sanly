package com.sanly.dmv.repository;

import com.sanly.dmv.entity.LicenseApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LicenseApplicationRepository extends JpaRepository<LicenseApplication, UUID> {

    Page<LicenseApplication> findByCitizenNationalIdOrderByAppliedAtDesc(
            String citizenNationalId, Pageable pageable);
}
