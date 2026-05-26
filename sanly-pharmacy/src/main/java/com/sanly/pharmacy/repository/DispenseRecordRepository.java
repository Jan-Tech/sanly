package com.sanly.pharmacy.repository;

import com.sanly.pharmacy.entity.DispenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DispenseRecordRepository extends JpaRepository<DispenseRecord, UUID> {
    List<DispenseRecord> findByPharmacyCodeOrderByDispensedAtDesc(String pharmacyCode);
    List<DispenseRecord> findByCitizenNationalIdAndPharmacyCodeOrderByDispensedAtDesc(
            String nationalId, String pharmacyCode);
    boolean existsByPrescriptionCode(String prescriptionCode);
}
