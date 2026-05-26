package com.sanly.analytics.repository;

import com.sanly.analytics.entity.RegionalStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionalStatRepository extends JpaRepository<RegionalStat, UUID> {
    Optional<RegionalStat> findByStatDateAndRegion(LocalDate date, String region);

    @Query("SELECT r FROM RegionalStat r WHERE r.statDate = (SELECT MAX(r2.statDate) FROM RegionalStat r2) ORDER BY r.citizenCount DESC")
    List<RegionalStat> findLatestAll();

    List<RegionalStat> findByStatDateBetweenOrderByStatDateDesc(LocalDate from, LocalDate to);
}
