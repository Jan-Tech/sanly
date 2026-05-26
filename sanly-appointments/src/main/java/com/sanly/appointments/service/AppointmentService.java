package com.sanly.appointments.service;

import com.sanly.appointments.client.NotificationClient;
import com.sanly.appointments.client.RegistryClient;
import com.sanly.appointments.dto.request.BookAppointmentRequest;
import com.sanly.appointments.dto.response.AppointmentResponse;
import com.sanly.appointments.dto.response.PageResponse;
import com.sanly.appointments.dto.response.StatsResponse;
import com.sanly.appointments.entity.*;
import com.sanly.appointments.repository.*;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final GovernmentOfficeRepository officeRepo;
    private final ServiceTypeRepository serviceTypeRepo;
    private final AvailabilityScheduleRepository scheduleRepo;
    private final AppointmentCodeService codeService;
    private final SlotCalculationService slotCalcService;
    private final RegistryClient registryClient;
    private final NotificationClient notificationClient;
    private final WaitlistService waitlistService;

    @Value("${rate-limit.book-capacity:10}")
    private long bookCapacity;

    @Value("${rate-limit.book-refill-hours:1}")
    private long bookRefillHours;

    private final ConcurrentHashMap<String, Bucket> citizenBuckets = new ConcurrentHashMap<>();

    @Transactional
    public AppointmentResponse book(BookAppointmentRequest req, String citizenNationalId, String citizenIp) {
        // Rate limit per citizen
        Bucket bucket = citizenBuckets.computeIfAbsent(citizenNationalId, id ->
                Bucket.builder()
                        .addLimit(Bandwidth.classic(bookCapacity,
                                Refill.intervally(bookCapacity, java.time.Duration.ofHours(bookRefillHours))))
                        .build()
        );
        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Booking rate limit exceeded. You may book up to " + bookCapacity + " appointments per hour.");
        }

        // Verify citizen with registry
        if (!registryClient.verifyCitizen(citizenNationalId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Citizen verification failed. Please ensure your national ID is registered.");
        }

        // Validate service type
        ServiceType serviceType = serviceTypeRepo.findById(req.getServiceTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service type not found."));
        if (serviceType.getStatus() != ServiceStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service type is not currently active.");
        }

        // Validate office
        GovernmentOffice office = officeRepo.findByOfficeCode(req.getOfficeCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Office not found."));
        if (office.getStatus() != OfficeStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Office is not currently active.");
        }

        // Check slot availability with pessimistic lock
        List<Appointment> activeAtSlot = appointmentRepo.findActiveAtSlot(
                req.getOfficeCode(), req.getAppointmentDate(), req.getSlotTime());

        // Get max concurrent from schedule
        ScheduleDay scheduleDay = toScheduleDay(req.getAppointmentDate().getDayOfWeek());
        AvailabilitySchedule schedule = scheduleRepo
                .findByOfficeCodeAndDayOfWeek(req.getOfficeCode(), scheduleDay)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No schedule available for this office on the selected day."));

        if (activeAtSlot.size() >= schedule.getMaxConcurrentAppointments()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot no longer available. Please choose a different time.");
        }

        // Generate code and save
        String appointmentCode = codeService.generateCode();

        Appointment appointment = Appointment.builder()
                .appointmentCode(appointmentCode)
                .citizenNationalId(citizenNationalId)
                .officeCode(req.getOfficeCode())
                .serviceTypeId(req.getServiceTypeId())
                .appointmentDate(req.getAppointmentDate())
                .slotTime(req.getSlotTime())
                .status(AppointmentStatus.BOOKED)
                .notes(req.getNotes())
                .build();

        Appointment saved = appointmentRepo.save(appointment);

        // Clear slot cache
        slotCalcService.clearCache(req.getOfficeCode(), req.getAppointmentDate());

        // Async notification
        notificationClient.send(
                citizenNationalId,
                "APPOINTMENT_BOOKED",
                "tk",
                Map.of(
                        "appointmentCode", appointmentCode,
                        "officeName", office.getName(),
                        "appointmentDate", req.getAppointmentDate().toString(),
                        "slotTime", req.getSlotTime().toString()
                )
        );

        return toResponse(saved);
    }

    public PageResponse<AppointmentResponse> getMyAppointments(String nationalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> apptPage = appointmentRepo
                .findByCitizenNationalIdOrderByAppointmentDateDescSlotTimeDesc(nationalId, pageable);
        Page<AppointmentResponse> responsePage = apptPage.map(this::toResponse);
        return PageResponse.from(responsePage);
    }

    public AppointmentResponse getMyAppointment(String nationalId, String appointmentCode) {
        Appointment appointment = findByCode(appointmentCode);
        checkOwnership(appointment, nationalId);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(String appointmentCode, String nationalId, String reason) {
        Appointment appointment = findByCode(appointmentCode);
        checkOwnership(appointment, nationalId);

        if (appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only BOOKED or CONFIRMED appointments can be cancelled.");
        }

        // Must cancel at least 2 hours before the appointment
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(), appointment.getSlotTime());
        if (LocalDateTime.now().isAfter(appointmentDateTime.minusHours(2))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Appointments must be cancelled at least 2 hours in advance.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        appointment.setCancelReason(reason);
        Appointment saved = appointmentRepo.save(appointment);

        // Clear slot cache
        slotCalcService.clearCache(appointment.getOfficeCode(), appointment.getAppointmentDate());

        // Async notification
        notificationClient.send(
                nationalId,
                "APPOINTMENT_CANCELLED_BY_CITIZEN",
                "tk",
                Map.of(
                        "appointmentCode", appointmentCode,
                        "reason", reason
                )
        );

        // Trigger waitlist check
        waitlistService.triggerCheck(appointment.getOfficeCode(), appointment.getServiceTypeId());

        return toResponse(saved);
    }

    @Transactional
    public AppointmentResponse rate(String appointmentCode, String nationalId, int rating, String feedback) {
        Appointment appointment = findByCode(appointmentCode);
        checkOwnership(appointment, nationalId);

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only COMPLETED appointments can be rated.");
        }

        appointment.setCitizenRating(rating);
        appointment.setCitizenFeedback(feedback);
        return toResponse(appointmentRepo.save(appointment));
    }

    public List<AppointmentResponse> getOfficeSchedule(String officeCode, LocalDate date) {
        return appointmentRepo.findByOfficeCodeAndAppointmentDateOrderBySlotTimeAsc(officeCode, date)
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED
                        || a.getStatus() == AppointmentStatus.CONFIRMED)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse complete(String appointmentCode, Long officerId) {
        Appointment appointment = findByCode(appointmentCode);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment is already completed.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot complete a cancelled appointment.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedByOfficerId(officerId);
        appointment.setCompletedAt(LocalDateTime.now());
        return toResponse(appointmentRepo.save(appointment));
    }

    @Transactional
    public AppointmentResponse markNoShow(String appointmentCode, Long officerId) {
        Appointment appointment = findByCode(appointmentCode);

        if (appointment.getStatus() != AppointmentStatus.BOOKED
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only BOOKED or CONFIRMED appointments can be marked as no-show.");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setCompletedByOfficerId(officerId);
        return toResponse(appointmentRepo.save(appointment));
    }

    public StatsResponse getStats(String officeCode) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        long totalBooked = appointmentRepo.countByOfficeCodeAndStatusAndDateFrom(
                officeCode, AppointmentStatus.BOOKED, thirtyDaysAgo);
        long totalCompleted = appointmentRepo.countByOfficeCodeAndStatusAndDateFrom(
                officeCode, AppointmentStatus.COMPLETED, thirtyDaysAgo);
        long totalCancelled = appointmentRepo.countByOfficeCodeAndStatusAndDateFrom(
                officeCode, AppointmentStatus.CANCELLED, thirtyDaysAgo);
        long totalNoShow = appointmentRepo.countByOfficeCodeAndStatusAndDateFrom(
                officeCode, AppointmentStatus.NO_SHOW, thirtyDaysAgo);

        long totalFinished = totalCompleted + totalCancelled + totalNoShow;
        double completionRate = totalFinished > 0 ? (double) totalCompleted / totalFinished * 100 : 0.0;
        double noShowRate = totalFinished > 0 ? (double) totalNoShow / totalFinished * 100 : 0.0;

        Double avgRating = appointmentRepo.avgRatingByOfficeCodeAndDateFrom(officeCode, thirtyDaysAgo);

        String busiestSlot = "N/A";
        List<Object[]> busiestSlotResult = appointmentRepo.findBusiestSlotByOfficeCode(
                officeCode, thirtyDaysAgo, PageRequest.of(0, 1));
        if (!busiestSlotResult.isEmpty()) {
            Object slotTime = busiestSlotResult.get(0)[0];
            busiestSlot = slotTime != null ? slotTime.toString() : "N/A";
        }

        return StatsResponse.builder()
                .totalBooked(totalBooked)
                .totalCompleted(totalCompleted)
                .totalCancelled(totalCancelled)
                .totalNoShow(totalNoShow)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .noShowRate(Math.round(noShowRate * 100.0) / 100.0)
                .averageRating(avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0)
                .busiestSlot(busiestSlot)
                .build();
    }

    public StatsResponse getPlatformStats() {
        long totalBooked = appointmentRepo.countByStatus(AppointmentStatus.BOOKED);
        long totalCompleted = appointmentRepo.countByStatus(AppointmentStatus.COMPLETED);
        long totalCancelled = appointmentRepo.countByStatus(AppointmentStatus.CANCELLED);
        long totalNoShow = appointmentRepo.countByStatus(AppointmentStatus.NO_SHOW);

        long totalFinished = totalCompleted + totalCancelled + totalNoShow;
        double completionRate = totalFinished > 0 ? (double) totalCompleted / totalFinished * 100 : 0.0;
        double noShowRate = totalFinished > 0 ? (double) totalNoShow / totalFinished * 100 : 0.0;

        Double avgRating = appointmentRepo.avgRatingOverall();

        String busiestSlot = "N/A";
        List<Object[]> busiestSlotResult = appointmentRepo.findBusiestSlotOverall(PageRequest.of(0, 1));
        if (!busiestSlotResult.isEmpty()) {
            Object slotTime = busiestSlotResult.get(0)[0];
            busiestSlot = slotTime != null ? slotTime.toString() : "N/A";
        }

        return StatsResponse.builder()
                .totalBooked(totalBooked)
                .totalCompleted(totalCompleted)
                .totalCancelled(totalCancelled)
                .totalNoShow(totalNoShow)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .noShowRate(Math.round(noShowRate * 100.0) / 100.0)
                .averageRating(avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0)
                .busiestSlot(busiestSlot)
                .build();
    }

    public PageResponse<AppointmentResponse> getAllAppointments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> apptPage = appointmentRepo.findAllByOrderByAppointmentDateDescSlotTimeDesc(pageable);
        Page<AppointmentResponse> responsePage = apptPage.map(this::toResponse);
        return PageResponse.from(responsePage);
    }

    private Appointment findByCode(String code) {
        return appointmentRepo.findByAppointmentCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Appointment not found: " + code));
    }

    private void checkOwnership(Appointment appointment, String nationalId) {
        if (!appointment.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have access to this appointment.");
        }
    }

    private AppointmentResponse toResponse(Appointment a) {
        String officeName = officeRepo.findByOfficeCode(a.getOfficeCode())
                .map(GovernmentOffice::getName)
                .orElse(null);

        String serviceName = null;
        if (a.getServiceTypeId() != null) {
            serviceName = serviceTypeRepo.findById(a.getServiceTypeId())
                    .map(ServiceType::getServiceName)
                    .orElse(null);
        }

        return AppointmentResponse.builder()
                .appointmentId(a.getAppointmentId())
                .appointmentCode(a.getAppointmentCode())
                .citizenNationalId(a.getCitizenNationalId())
                .officeCode(a.getOfficeCode())
                .officeName(officeName)
                .serviceTypeId(a.getServiceTypeId())
                .serviceName(serviceName)
                .appointmentDate(a.getAppointmentDate())
                .slotTime(a.getSlotTime())
                .status(a.getStatus())
                .bookedAt(a.getBookedAt())
                .notes(a.getNotes())
                .reminderSent(a.isReminderSent())
                .cancelledAt(a.getCancelledAt())
                .cancelReason(a.getCancelReason())
                .completedByOfficerId(a.getCompletedByOfficerId())
                .completedAt(a.getCompletedAt())
                .citizenRating(a.getCitizenRating())
                .citizenFeedback(a.getCitizenFeedback())
                .build();
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
