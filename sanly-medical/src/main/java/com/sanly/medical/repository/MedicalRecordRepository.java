package com.sanly.medical.repository;

import com.sanly.medical.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    /** All records for a citizen — admin only (no clinic filter). */
    Page<MedicalRecord> findByCitizenNationalIdOrderByTestedAtDesc(
            String nationalId, Pageable pageable);

    /** Records for a citizen scoped to the doctor's clinic. */
    Page<MedicalRecord> findByCitizenNationalIdAndClinicIdOrderByTestedAtDesc(
            String nationalId, Long clinicId, Pageable pageable);

    /** Find a specific record, scoped to a clinic (for clinic-level access control). */
    Optional<MedicalRecord> findByRecordIdAndClinicId(UUID recordId, Long clinicId);

    /** Records that haven't been published to SANLY Bridge yet (for re-try jobs). */
    @Query("SELECT r FROM MedicalRecord r WHERE r.bridgePublished = false ORDER BY r.createdAt ASC")
    List<MedicalRecord> findUnpublished();
}
