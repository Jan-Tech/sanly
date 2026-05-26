package com.sanly.social.repository;

import com.sanly.social.entity.UnemploymentRecord;
import com.sanly.social.entity.UnemploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnemploymentRecordRepository extends JpaRepository<UnemploymentRecord, UUID> {
    Optional<UnemploymentRecord> findByCitizenNationalId(String citizenNationalId);
    boolean existsByCitizenNationalId(String citizenNationalId);
    List<UnemploymentRecord> findByStatus(UnemploymentStatus status);

    // Registered more than 12 months ago (for expiry job)
    @Query("SELECT r FROM UnemploymentRecord r WHERE r.status = 'REGISTERED' AND r.registeredAt <= :cutoff")
    List<UnemploymentRecord> findExpiredRegistrations(LocalDateTime cutoff);
}
