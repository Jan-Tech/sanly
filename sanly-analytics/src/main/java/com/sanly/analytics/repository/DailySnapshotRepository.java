package com.sanly.analytics.repository;

import com.sanly.analytics.entity.DailySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailySnapshotRepository extends JpaRepository<DailySnapshot, UUID> {
    Optional<DailySnapshot> findBySnapshotDate(LocalDate date);
    List<DailySnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate from, LocalDate to);
    List<DailySnapshot> findTop30ByOrderBySnapshotDateDesc();
}
