package com.sanly.vehicle.service;

import com.sanly.vehicle.client.NotificationClient;
import com.sanly.vehicle.dto.response.OwnershipResponse;
import com.sanly.vehicle.entity.*;
import com.sanly.vehicle.repository.VehicleOwnershipRepository;
import com.sanly.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnershipService {
    private final VehicleOwnershipRepository ownershipRepo;
    private final VehicleRepository vehicleRepo;
    private final NotificationClient notificationClient;

    @Transactional(readOnly = true)
    public List<OwnershipResponse> findByPlate(String plateNumber) {
        return ownershipRepo.findByPlateNumberOrderByOwnershipStartDateDesc(plateNumber)
                .stream().map(this::toResponse).toList();
    }

    /** Called by civil service on death registration — marks all ACTIVE ownerships as INHERITED. */
    @Transactional
    public void handleDeceased(String deceasedNationalId) {
        List<VehicleOwnership> active = ownershipRepo.findByOwnerNationalIdAndStatus(
                deceasedNationalId, OwnershipStatus.ACTIVE);
        for (VehicleOwnership ownership : active) {
            ownership.setStatus(OwnershipStatus.INHERITED);
            ownership.setOwnershipEndDate(LocalDate.now());
            ownershipRepo.save(ownership);

            vehicleRepo.findByPlateNumber(ownership.getPlateNumber()).ifPresent(v -> {
                v.setStatus(VehicleStatus.UNDER_TRANSFER);
                vehicleRepo.save(v);
            });

            log.info("[VEHICLE] Ownership of plate={} set to INHERITED due to death of NIN={}",
                    ownership.getPlateNumber(), deceasedNationalId);
        }

        if (!active.isEmpty()) {
            String plates = active.stream().map(VehicleOwnership::getPlateNumber)
                    .reduce((a, b) -> a + ", " + b).orElse("");
            notificationClient.send(deceasedNationalId, "VEHICLE_INHERITANCE_PENDING", "EN",
                    Map.of("plateNumber", plates));
        }
    }

    private OwnershipResponse toResponse(VehicleOwnership o) {
        return new OwnershipResponse(o.getOwnershipId(), o.getPlateNumber(), o.getOwnerNationalId(),
                o.getOwnerBusinessNumber(), o.getOwnershipStartDate(), o.getOwnershipEndDate(),
                o.getAcquiredVia().name(), o.getStatus().name());
    }
}
