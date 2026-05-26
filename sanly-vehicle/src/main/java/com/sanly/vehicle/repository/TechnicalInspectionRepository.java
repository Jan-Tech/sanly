package com.sanly.vehicle.repository;

import com.sanly.vehicle.entity.TechnicalInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TechnicalInspectionRepository extends JpaRepository<TechnicalInspection, Long> {
    List<TechnicalInspection> findByPlateNumberOrderByInspectionDateDesc(String plateNumber);
    Optional<TechnicalInspection> findFirstByPlateNumberOrderByInspectionDateDesc(String plateNumber);

    @Query("SELECT t FROM TechnicalInspection t WHERE t.nextInspectionDue BETWEEN :today AND :cutoff " +
           "AND t.inspectionId = (SELECT MAX(t2.inspectionId) FROM TechnicalInspection t2 WHERE t2.plateNumber = t.plateNumber)")
    List<TechnicalInspection> findDueBetween(@Param("today") LocalDate today, @Param("cutoff") LocalDate cutoff);
}
