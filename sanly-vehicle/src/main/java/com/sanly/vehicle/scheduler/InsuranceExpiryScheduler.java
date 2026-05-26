package com.sanly.vehicle.scheduler;

import com.sanly.vehicle.client.NotificationClient;
import com.sanly.vehicle.entity.InsuranceRecord;
import com.sanly.vehicle.entity.OwnershipStatus;
import com.sanly.vehicle.entity.VehicleOwnership;
import com.sanly.vehicle.repository.InsuranceRecordRepository;
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
public class InsuranceExpiryScheduler {
    private final InsuranceRecordRepository insuranceRepo;
    private final VehicleOwnershipRepository ownershipRepo;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 8 * * *")
    public void notifyExpiringInsurance() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(14);
        List<InsuranceRecord> expiring = insuranceRepo.findExpiringBetween(today, cutoff);
        log.info("[INSURANCE-SCHEDULER] Found {} insurance records expiring within 14 days", expiring.size());

        for (InsuranceRecord ins : expiring) {
            Optional<VehicleOwnership> ownership = ownershipRepo.findByPlateNumberAndStatus(
                    ins.getPlateNumber(), OwnershipStatus.ACTIVE);
            if (ownership.isEmpty() || ownership.get().getOwnerNationalId() == null) continue;

            notificationClient.send(ownership.get().getOwnerNationalId(), "INSURANCE_EXPIRING_SOON", "EN",
                    Map.of("plateNumber", ins.getPlateNumber(), "expiryDate", ins.getValidUntil().toString()));
        }
    }
}
