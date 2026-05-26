package com.sanly.vehicle.repository;

import com.sanly.vehicle.entity.InsuranceRecord;
import com.sanly.vehicle.entity.InsuranceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InsuranceRecordRepository extends JpaRepository<InsuranceRecord, Long> {
    List<InsuranceRecord> findByPlateNumberOrderByValidFromDesc(String plateNumber);
    Optional<InsuranceRecord> findByPlateNumberAndStatus(String plateNumber, InsuranceStatus status);

    @Query("SELECT i FROM InsuranceRecord i WHERE i.status = 'ACTIVE' AND i.validUntil BETWEEN :today AND :cutoff")
    List<InsuranceRecord> findExpiringBetween(@Param("today") LocalDate today, @Param("cutoff") LocalDate cutoff);
}
