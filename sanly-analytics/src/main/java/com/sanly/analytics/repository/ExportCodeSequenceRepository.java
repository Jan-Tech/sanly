package com.sanly.analytics.repository;

import com.sanly.analytics.entity.ExportCodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExportCodeSequenceRepository extends JpaRepository<ExportCodeSequence, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ExportCodeSequence s WHERE s.year = :year")
    Optional<ExportCodeSequence> findByYearWithLock(int year);
}
