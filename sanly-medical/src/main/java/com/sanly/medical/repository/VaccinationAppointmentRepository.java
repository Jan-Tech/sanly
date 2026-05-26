package com.sanly.medical.repository;

import com.sanly.medical.entity.VaccinationAppointment;
import com.sanly.medical.entity.VaccinationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VaccinationAppointmentRepository extends JpaRepository<VaccinationAppointment, UUID> {

    List<VaccinationAppointment> findByScheduleIdOrderByDueDateAsc(UUID scheduleId);

    List<VaccinationAppointment> findByCitizenNationalIdOrderByDueDateAsc(String citizenNationalId);

    /** Finds PENDING appointments due within the next {@code days} days — used by daily reminder job. */
    @Query("SELECT a FROM VaccinationAppointment a WHERE a.status = 'PENDING' " +
           "AND a.dueDate >= :today AND a.dueDate <= :until")
    List<VaccinationAppointment> findDueSoon(@Param("today") LocalDate today,
                                              @Param("until") LocalDate until);
}
