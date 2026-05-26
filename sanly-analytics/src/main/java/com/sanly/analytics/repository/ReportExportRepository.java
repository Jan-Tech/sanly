package com.sanly.analytics.repository;

import com.sanly.analytics.entity.ReportExport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportExportRepository extends JpaRepository<ReportExport, UUID> {
    Optional<ReportExport> findByExportCode(String exportCode);
    Page<ReportExport> findByGeneratedByOfficerIdOrderByGeneratedAtDesc(UUID officerId, Pageable pageable);
    Page<ReportExport> findAllByOrderByGeneratedAtDesc(Pageable pageable);
    List<ReportExport> findByStatus(String status);
}
