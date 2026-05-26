package com.sanly.appointments.service;

import com.sanly.appointments.dto.request.AddExceptionRequest;
import com.sanly.appointments.dto.request.SetScheduleRequest;
import com.sanly.appointments.entity.AvailabilityException;
import com.sanly.appointments.entity.AvailabilitySchedule;
import com.sanly.appointments.repository.AvailabilityExceptionRepository;
import com.sanly.appointments.repository.AvailabilityScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final AvailabilityScheduleRepository scheduleRepo;
    private final AvailabilityExceptionRepository exceptionRepo;

    @Transactional
    public AvailabilitySchedule setSchedule(SetScheduleRequest req) {
        AvailabilitySchedule schedule = scheduleRepo
                .findByOfficeCodeAndDayOfWeek(req.getOfficeCode(), req.getDayOfWeek())
                .orElseGet(() -> AvailabilitySchedule.builder()
                        .officeCode(req.getOfficeCode())
                        .dayOfWeek(req.getDayOfWeek())
                        .build());
        schedule.setOpenTime(req.getOpenTime());
        schedule.setCloseTime(req.getCloseTime());
        schedule.setSlotDurationMinutes(req.getSlotDurationMinutes() > 0 ? req.getSlotDurationMinutes() : 30);
        schedule.setMaxConcurrentAppointments(req.getMaxConcurrentAppointments() > 0 ? req.getMaxConcurrentAppointments() : 3);
        schedule.setActive(true);
        return scheduleRepo.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySchedule> getSchedule(String officeCode) {
        return scheduleRepo.findByOfficeCode(officeCode);
    }

    @Transactional
    public AvailabilityException addException(AddExceptionRequest req) {
        AvailabilityException ex = AvailabilityException.builder()
                .officeCode(req.getOfficeCode())
                .exceptionDate(req.getExceptionDate())
                .reason(req.getReason())
                .isClosed(req.isClosed())
                .alternateOpenTime(req.getAlternateOpenTime())
                .alternateCloseTime(req.getAlternateCloseTime())
                .build();
        return exceptionRepo.save(ex);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityException> getExceptions(String officeCode) {
        return exceptionRepo.findByOfficeCodeAndExceptionDateBetween(
                officeCode, LocalDate.now(), LocalDate.now().plusDays(90));
    }
}
