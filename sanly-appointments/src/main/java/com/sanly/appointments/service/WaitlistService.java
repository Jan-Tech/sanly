package com.sanly.appointments.service;

import com.sanly.appointments.client.NotificationClient;
import com.sanly.appointments.dto.request.WaitlistRequest;
import com.sanly.appointments.entity.GovernmentOffice;
import com.sanly.appointments.entity.ServiceType;
import com.sanly.appointments.entity.WaitlistEntry;
import com.sanly.appointments.entity.WaitlistStatus;
import com.sanly.appointments.repository.GovernmentOfficeRepository;
import com.sanly.appointments.repository.ServiceTypeRepository;
import com.sanly.appointments.repository.WaitlistEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistEntryRepository waitlistRepo;
    private final GovernmentOfficeRepository officeRepo;
    private final ServiceTypeRepository serviceTypeRepo;
    private final NotificationClient notificationClient;

    @Transactional
    public WaitlistEntry join(WaitlistRequest req, String nationalId) {
        officeRepo.findByOfficeCode(req.getOfficeCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Office not found."));

        WaitlistEntry entry = WaitlistEntry.builder()
                .citizenNationalId(nationalId)
                .officeCode(req.getOfficeCode())
                .serviceTypeId(req.getServiceTypeId())
                .preferredDateFrom(req.getPreferredDateFrom())
                .preferredDateTo(req.getPreferredDateTo())
                .status(WaitlistStatus.WAITING)
                .build();
        return waitlistRepo.save(entry);
    }

    @Transactional
    public void leave(UUID waitlistId, String nationalId) {
        WaitlistEntry entry = waitlistRepo.findById(waitlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waitlist entry not found."));
        if (!entry.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your waitlist entry.");
        }
        waitlistRepo.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<WaitlistEntry> getCitizenWaitlist(String nationalId) {
        return waitlistRepo.findByCitizenNationalId(nationalId);
    }

    /** Called when a slot opens (cancellation) — notifies first WAITING citizen. */
    @Transactional
    public void triggerCheck(String officeCode, UUID serviceTypeId) {
        List<WaitlistEntry> waiting = serviceTypeId != null
                ? waitlistRepo.findByOfficeCodeAndServiceTypeIdAndStatusOrderByCreatedAtAsc(
                        officeCode, serviceTypeId, WaitlistStatus.WAITING)
                : waitlistRepo.findByOfficeCodeAndStatusOrderByCreatedAtAsc(officeCode, WaitlistStatus.WAITING);

        if (waiting.isEmpty()) return;

        WaitlistEntry first = waiting.get(0);
        first.setStatus(WaitlistStatus.NOTIFIED);
        first.setNotifiedAt(LocalDateTime.now());
        waitlistRepo.save(first);

        String officeName = officeRepo.findByOfficeCode(officeCode)
                .map(GovernmentOffice::getName).orElse(officeCode);
        String serviceName = serviceTypeId != null
                ? serviceTypeRepo.findById(serviceTypeId).map(ServiceType::getServiceName).orElse("")
                : "";

        notificationClient.send(first.getCitizenNationalId(), "WAITLIST_SLOT_AVAILABLE", "EN",
                Map.of("officeName", officeName, "serviceName", serviceName,
                        "date", "soon", "time", "available"));
        log.info("Waitlist notified: officeCode={} NIN={}", officeCode, first.getCitizenNationalId());
    }

    /** Resets NOTIFIED entries older than 2 hours back to WAITING and notifies next citizen. */
    @Transactional
    public void processExpiredNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
        List<WaitlistEntry> expired = waitlistRepo.findByStatusAndNotifiedAtBefore(WaitlistStatus.NOTIFIED, cutoff);
        for (WaitlistEntry entry : expired) {
            entry.setStatus(WaitlistStatus.WAITING);
            entry.setNotifiedAt(null);
            waitlistRepo.save(entry);
            log.info("Waitlist notification expired for NIN={}, resetting to WAITING", entry.getCitizenNationalId());
            // Trigger next citizen
            triggerCheck(entry.getOfficeCode(), entry.getServiceTypeId());
        }
    }

    /** Expires WAITING entries older than 30 days. Called by scheduler. */
    @Transactional
    public int expireOldEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<WaitlistEntry> old = waitlistRepo.findByStatusAndCreatedAtBefore(WaitlistStatus.WAITING, cutoff);
        for (WaitlistEntry entry : old) {
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistRepo.save(entry);
            String officeName = officeRepo.findByOfficeCode(entry.getOfficeCode())
                    .map(GovernmentOffice::getName).orElse(entry.getOfficeCode());
            notificationClient.send(entry.getCitizenNationalId(), "WAITLIST_EXPIRED", "EN",
                    Map.of("officeName", officeName));
        }
        log.info("Expired {} waitlist entries", old.size());
        return old.size();
    }
}
