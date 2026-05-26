package com.sanly.medical.repository;

import com.sanly.medical.entity.VaccinationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VaccinationScheduleRepository extends JpaRepository<VaccinationSchedule, UUID> {
    Optional<VaccinationSchedule> findByCitizenNationalId(String citizenNationalId);
    boolean existsByCitizenNationalId(String citizenNationalId);
}
