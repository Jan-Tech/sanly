package com.sanly.vehicle.scheduler;

import com.sanly.vehicle.client.NotificationClient;
import com.sanly.vehicle.entity.OwnershipStatus;
import com.sanly.vehicle.entity.TechnicalInspection;
import com.sanly.vehicle.entity.VehicleOwnership;
import com.sanly.vehicle.repository.TechnicalInspectionRepository;
import com.sanly.vehicle.repository.VehicleOwnershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectionDueScheduler {
    private final TechnicalInspectionRepository inspectionRepo;
    private final VehicleOwnershipRepository ownershipRepo;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 30 8 * * *")
    public void notifyInspectionDue() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(30);
        List<TechnicalInspection> due = inspectionRepo.findDueBetween(today, cutoff);
        log.info("[INSPECTION-SCHEDULER] Found {} vehicles with inspection due within 30 days", due.size());

        for (TechnicalInspection insp : due) {
            Optional<VehicleOwnership> ownership = ownershipRepo.findByPlateNumberAndStatus(
                    insp.getPlateNumber(), OwnershipStatus.ACTIVE);
            if (ownership.isEmpty() || ownership.get().getOwnerNationalId() == null) continue;

            notificationClient.send(ownership.get().getOwnerNationalId(), "INSPECTION_DUE_SOON", "EN",
                    Map.of("plateNumber", insp.getPlateNumber(), "dueDate", insp.getNextInspectionDue().toString()));
        }
    }
}
