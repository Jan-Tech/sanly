package com.sanly.tax.repository;

import com.sanly.tax.entity.FilingStatus;
import com.sanly.tax.entity.TaxFiling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxFilingRepository extends JpaRepository<TaxFiling, UUID> {
    List<TaxFiling> findByTaxIdOrderByTaxYearDesc(String taxId);
    Optional<TaxFiling> findTopByTaxIdOrderByTaxYearDescSubmittedAtDesc(String taxId);
}
