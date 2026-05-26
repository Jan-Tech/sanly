package com.sanly.business.repository;

import com.sanly.business.entity.Business;
import com.sanly.business.entity.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    Optional<Business> findByRegistrationNumber(String registrationNumber);
    boolean existsByOwnerNationalId(String ownerNationalId);
    Page<Business> findByStatus(BusinessStatus status, Pageable pageable);
    Page<Business> findByOwnerNationalId(String ownerNationalId, Pageable pageable);
}
