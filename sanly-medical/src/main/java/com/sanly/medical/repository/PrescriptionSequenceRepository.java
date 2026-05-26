package com.sanly.medical.repository;

import com.sanly.medical.entity.PrescriptionSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrescriptionSequenceRepository extends JpaRepository<PrescriptionSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM PrescriptionSequence s WHERE s.year = :year")
    Optional<PrescriptionSequence> findByYearForUpdate(@Param("year") Integer year);
}
