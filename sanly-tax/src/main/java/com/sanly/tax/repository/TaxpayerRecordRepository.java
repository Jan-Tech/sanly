package com.sanly.tax.repository;

import com.sanly.tax.entity.TaxpayerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxpayerRecordRepository extends JpaRepository<TaxpayerRecord, UUID> {
    Optional<TaxpayerRecord> findByCitizenNationalId(String citizenNationalId);
    Optional<TaxpayerRecord> findByTaxId(String taxId);
    boolean existsByCitizenNationalId(String citizenNationalId);
}
