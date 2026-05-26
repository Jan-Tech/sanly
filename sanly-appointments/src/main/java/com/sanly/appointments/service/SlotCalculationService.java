package com.sanly.appointments.service;

import com.sanly.appointments.dto.response.SlotResponse;
import com.sanly.appointments.entity.AvailabilityException;
import com.sanly.appointments.entity.AvailabilitySchedule;
import com.sanly.appointments.entity.ScheduleDay;
import com.sanly.appointments.repository.AvailabilityExceptionRepository;
import com.sanly.appointments.repository.AvailabilityScheduleRepository;
import com.sanly.appointments.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotCalculationService {

    private final AvailabilityScheduleRepository scheduleRepo;
    private final AvailabilityExceptionRepository exceptionRepo;
    private final AppointmentRepository appointmentRepo;

    private final ConcurrentHashMap<String, CachedSlots> cache = new ConcurrentHashMap<>();

    private static final long CACHE_TTL_SECONDS = 60;

    private static class CachedSlots {
        final List<SlotResponse> slots;
        final LocalDateTime cachedAt;

        CachedSlots(List<SlotResponse> slots) {
            this.slots = slots;
            this.cachedAt = LocalDateTime.now();
        }

        boolean isStale() {
            return LocalDateTime.now().isAfter(cachedAt.plusSeconds(CACHE_TTL_SECONDS));
        }
    }

    public List<SlotResponse> getAvailableSlots(String officeCode, LocalDate date, UUID serviceTypeId) {
        String cacheKey = officeCode + ":" + date + ":" + serviceTypeId;

        CachedSlots cached = cache.get(cacheKey);
        if (cached != null && !cached.isStale()) {
            return cached.slots;
        }

        List<SlotResponse> slots = computeSlots(officeCode, date);
        cache.put(cacheKey, new CachedSlots(slots));
        return slots;
    }

    private List<SlotResponse> computeSlots(String officeCode, LocalDate date) {
        ScheduleDay scheduleDay = toScheduleDay(date.getDayOfWeek());
        Optional<AvailabilitySchedule> scheduleOpt =
                scheduleRepo.findByOfficeCodeAndDayOfWeek(officeCode, scheduleDay);

        if (scheduleOpt.isEmpty() || !scheduleOpt.get().isActive()) {
            return List.of();
        }
        AvailabilitySchedule schedule = scheduleOpt.get();

        Optional<AvailabilityException> exceptionOpt =
                exceptionRepo.findByOfficeCodeAndExceptionDate(officeCode, date);

        if (exceptionOpt.isPresent() && exceptionOpt.get().isClosed()) {
            return List.of();
        }

        LocalTime openTime;
        LocalTime closeTime;

        if (exceptionOpt.isPresent() && exceptionOpt.get().getAlternateOpenTime() != null) {
            openTime = exceptionOpt.get().getAlternateOpenTime();
            closeTime = exceptionOpt.get().getAlternateCloseTime();
        } else {
            openTime = schedule.getOpenTime();
            closeTime = schedule.getCloseTime();
        }

        int slotDuration = schedule.getSlotDurationMinutes();
        int maxConcurrent = schedule.getMaxConcurrentAppointments();

        List<SlotResponse> slots = new ArrayList<>();
        LocalTime slotStart = openTime;

        while (!slotStart.plusMinutes(slotDuration).isAfter(closeTime)) {
            long booked = appointmentRepo.countActiveAtSlot(officeCode, date, slotStart);
            int spotsRemaining = (int) Math.max(0, maxConcurrent - booked);
            boolean available = spotsRemaining > 0;

            slots.add(SlotResponse.builder()
                    .slotTime(slotStart)
                    .available(available)
                    .spotsRemaining(spotsRemaining)
                    .build());

            slotStart = slotStart.plusMinutes(slotDuration);
        }

        return slots;
    }

    public List<LocalDate> getNextAvailableDates(String officeCode, UUID serviceTypeId, LocalDate from) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate current = from;
        int daysChecked = 0;

        while (result.size() < 5 && daysChecked < 60) {
            List<SlotResponse> slots = getAvailableSlots(officeCode, current, serviceTypeId);
            boolean hasAvailable = slots.stream().anyMatch(SlotResponse::isAvailable);
            if (hasAvailable) {
                result.add(current);
            }
            current = current.plusDays(1);
            daysChecked++;
        }

        return result;
    }

    public void clearCache(String officeCode, LocalDate date) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(officeCode + ":" + date + ":"));
    }

    private ScheduleDay toScheduleDay(java.time.DayOfWeek javaDayOfWeek) {
        return switch (javaDayOfWeek) {
            case MONDAY -> ScheduleDay.MON;
            case TUESDAY -> ScheduleDay.TUE;
            case WEDNESDAY -> ScheduleDay.WED;
            case THURSDAY -> ScheduleDay.THU;
            case FRIDAY -> ScheduleDay.FRI;
            case SATURDAY -> ScheduleDay.SAT;
            case SUNDAY -> ScheduleDay.SUN;
        };
    }
}
