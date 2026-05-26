package com.sanly.medical.repository;

import com.sanly.medical.entity.Prescription;
import com.sanly.medical.entity.PrescriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    Optional<Prescription> findByPrescriptionCode(String prescriptionCode);

    List<Prescription> findByCitizenNationalIdOrderByIssuedAtDesc(String nationalId);

    List<Prescription> findByCitizenNationalIdAndStatusOrderByIssuedAtDesc(
            String nationalId, PrescriptionStatus status);

    /** Pessimistic write lock for dispense operation — prevents concurrent dispensing races. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prescription p WHERE p.prescriptionCode = :code")
    Optional<Prescription> findByPrescriptionCodeForUpdate(@Param("code") String code);
}
