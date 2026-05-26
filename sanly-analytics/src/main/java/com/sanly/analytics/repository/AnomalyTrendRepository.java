package com.sanly.analytics.repository;

import com.sanly.analytics.entity.AnomalyTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnomalyTrendRepository extends JpaRepository<AnomalyTrend, UUID> {
    Optional<AnomalyTrend> findByStatDateAndInstitutionCodeAndAlertType(LocalDate date, String institution, String alertType);
    List<AnomalyTrend> findByStatDateBetweenOrderByStatDateDesc(LocalDate from, LocalDate to);

    @Query("SELECT a FROM AnomalyTrend a WHERE a.statDate >= :from ORDER BY a.alertCount DESC")
    List<AnomalyTrend> findTopAlertsByDate(LocalDate from);
}
