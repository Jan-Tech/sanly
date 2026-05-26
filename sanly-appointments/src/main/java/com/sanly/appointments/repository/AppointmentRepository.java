package com.sanly.appointments.repository;

import com.sanly.appointments.entity.Appointment;
import com.sanly.appointments.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByAppointmentCode(String appointmentCode);

    Page<Appointment> findByCitizenNationalIdOrderByAppointmentDateDescSlotTimeDesc(
            String citizenNationalId, Pageable pageable);

    List<Appointment> findByOfficeCodeAndAppointmentDateOrderBySlotTimeAsc(
            String officeCode, LocalDate appointmentDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.officeCode = :officeCode " +
           "AND a.appointmentDate = :date AND a.slotTime = :slotTime " +
           "AND a.status IN (com.sanly.appointments.entity.AppointmentStatus.BOOKED, " +
           "com.sanly.appointments.entity.AppointmentStatus.CONFIRMED)")
    List<Appointment> findActiveAtSlot(@Param("officeCode") String officeCode,
                                        @Param("date") LocalDate date,
                                        @Param("slotTime") LocalTime slotTime);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.officeCode = :officeCode " +
           "AND a.appointmentDate = :date AND a.slotTime = :slotTime " +
           "AND a.status IN (com.sanly.appointments.entity.AppointmentStatus.BOOKED, " +
           "com.sanly.appointments.entity.AppointmentStatus.CONFIRMED)")
    long countActiveAtSlot(@Param("officeCode") String officeCode,
                            @Param("date") LocalDate date,
                            @Param("slotTime") LocalTime slotTime);

    List<Appointment> findByStatusAndAppointmentDateAndReminderSentFalse(
            AppointmentStatus status, LocalDate appointmentDate);

    @Modifying
    @Query("UPDATE Appointment a SET a.status = com.sanly.appointments.entity.AppointmentStatus.NO_SHOW " +
           "WHERE a.status = com.sanly.appointments.entity.AppointmentStatus.BOOKED " +
           "AND a.appointmentDate = :date AND a.slotTime <= :cutoff")
    int markNoShows(@Param("date") LocalDate date, @Param("cutoff") LocalTime cutoff);

    Page<Appointment> findAllByOrderByAppointmentDateDescSlotTimeDesc(Pageable pageable);

    // Stats queries
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.officeCode = :officeCode " +
           "AND a.appointmentDate >= :fromDate AND a.status = :status")
    long countByOfficeCodeAndStatusAndDateFrom(@Param("officeCode") String officeCode,
                                               @Param("status") AppointmentStatus status,
                                               @Param("fromDate") LocalDate fromDate);

    @Query("SELECT AVG(a.citizenRating) FROM Appointment a WHERE a.officeCode = :officeCode " +
           "AND a.citizenRating IS NOT NULL AND a.appointmentDate >= :fromDate")
    Double avgRatingByOfficeCodeAndDateFrom(@Param("officeCode") String officeCode,
                                            @Param("fromDate") LocalDate fromDate);

    @Query("SELECT a.slotTime, COUNT(a) as cnt FROM Appointment a WHERE a.officeCode = :officeCode " +
           "AND a.appointmentDate >= :fromDate " +
           "GROUP BY a.slotTime ORDER BY cnt DESC")
    List<Object[]> findBusiestSlotByOfficeCode(@Param("officeCode") String officeCode,
                                               @Param("fromDate") LocalDate fromDate,
                                               Pageable pageable);

    // Platform-wide stats
    long countByStatus(AppointmentStatus status);

    @Query("SELECT AVG(a.citizenRating) FROM Appointment a WHERE a.citizenRating IS NOT NULL")
    Double avgRatingOverall();

    @Query("SELECT a.slotTime, COUNT(a) as cnt FROM Appointment a " +
           "GROUP BY a.slotTime ORDER BY cnt DESC")
    List<Object[]> findBusiestSlotOverall(Pageable pageable);
}
