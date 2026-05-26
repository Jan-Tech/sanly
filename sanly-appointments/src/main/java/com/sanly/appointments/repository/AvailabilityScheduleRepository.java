package com.sanly.appointments.repository;

import com.sanly.appointments.entity.AvailabilitySchedule;
import com.sanly.appointments.entity.ScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityScheduleRepository extends JpaRepository<AvailabilitySchedule, Long> {

    List<AvailabilitySchedule> findByOfficeCode(String officeCode);

    Optional<AvailabilitySchedule> findByOfficeCodeAndDayOfWeek(String officeCode, ScheduleDay dayOfWeek);
}
