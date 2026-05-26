package com.sanly.vehicle.service;

import com.sanly.vehicle.client.BridgePublisherService;
import com.sanly.vehicle.client.CitizenRegistryClient;
import com.sanly.vehicle.client.NotificationClient;
import com.sanly.vehicle.dto.request.RegisterVehicleRequest;
import com.sanly.vehicle.dto.request.UpdateVehicleStatusRequest;
import com.sanly.vehicle.dto.response.VehicleResponse;
import com.sanly.vehicle.dto.response.VehicleVerifyResponse;
import com.sanly.vehicle.entity.*;
import com.sanly.vehicle.exception.BusinessException;
import com.sanly.vehicle.exception.ResourceNotFoundException;
import com.sanly.vehicle.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleOwnershipRepository ownershipRepository;
    private final InsuranceRecordRepository insuranceRepository;
    private final TechnicalInspectionRepository inspectionRepository;
    private final PlateNumberService plateNumberService;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;

    @Transactional
    public VehicleResponse register(RegisterVehicleRequest req) {
        if (req.getOwnerNationalId() == null && req.getOwnerBusinessNumber() == null)
            throw new BusinessException("Either ownerNationalId or ownerBusinessNumber is required");
        if (vehicleRepository.existsByVin(req.getVin()))
            throw new BusinessException("VIN already registered: " + req.getVin());

        if (req.getOwnerNationalId() != null)
            citizenRegistryClient.verify(req.getOwnerNationalId());

        String plate = plateNumberService.nextPlateNumber();

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .plateNumber(plate)
                .vin(req.getVin())
                .make(req.getMake())
                .model(req.getModel())
                .year(req.getYear())
                .color(req.getColor())
                .engineVolume(req.getEngineVolume())
                .fuelType(req.getFuelType())
                .vehicleType(req.getVehicleType())
                .registeredAt(LocalDateTime.now())
                .status(VehicleStatus.REGISTERED)
                .build());

        ownershipRepository.save(VehicleOwnership.builder()
                .plateNumber(plate)
                .ownerNationalId(req.getOwnerNationalId())
                .ownerBusinessNumber(req.getOwnerBusinessNumber())
                .ownershipStartDate(LocalDate.now())
                .acquiredVia(AcquiredVia.PURCHASE)
                .status(OwnershipStatus.ACTIVE)
                .build());

        bridgePublisherService.publishVehicleRecord(vehicle, req.getOwnerNationalId(), null, null);

        if (req.getOwnerNationalId() != null) {
            notificationClient.send(req.getOwnerNationalId(), "VEHICLE_REGISTERED", "EN", Map.of(
                    "make", vehicle.getMake(),
                    "model", vehicle.getModel(),
                    "year", String.valueOf(vehicle.getYear()),
                    "plateNumber", plate
            ));
        }
        return toResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getByPlate(String plateNumber) {
        return toResponse(findOrThrow(plateNumber));
    }

    @Transactional(readOnly = true)
    public VehicleResponse getByVin(String vin) {
        Vehicle v = vehicleRepository.findByVin(vin)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found for VIN: " + vin));
        return toResponse(v);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getByOwner(String nationalId) {
        return ownershipRepository.findByOwnerNationalIdAndStatus(nationalId, OwnershipStatus.ACTIVE).stream()
                .map(o -> vehicleRepository.findByPlateNumber(o.getPlateNumber()).map(this::toResponse).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleVerifyResponse verify(String plateNumber) {
        Vehicle v = findOrThrow(plateNumber);
        VehicleOwnership ownership = ownershipRepository.findByPlateNumberAndStatus(plateNumber, OwnershipStatus.ACTIVE).orElse(null);
        InsuranceRecord ins = insuranceRepository.findByPlateNumberAndStatus(plateNumber, InsuranceStatus.ACTIVE).orElse(null);
        TechnicalInspection insp = inspectionRepository.findFirstByPlateNumberOrderByInspectionDateDesc(plateNumber).orElse(null);

        return new VehicleVerifyResponse(
                v.getPlateNumber(), v.getVin(), v.getMake(), v.getModel(), v.getYear(), v.getColor(),
                v.getVehicleType().name(), v.getFuelType().name(), v.getStatus().name(),
                ownership != null ? ownership.getOwnerNationalId() : null,
                ownership != null ? ownership.getOwnerBusinessNumber() : null,
                ins != null ? ins.getStatus().name() : "NONE",
                ins != null ? ins.getValidUntil() : null,
                insp != null ? insp.getInspectionDate() : null,
                insp != null ? insp.getNextInspectionDue() : null,
                insp != null ? insp.getResult().name() : null
        );
    }

    @Transactional
    public VehicleResponse updateStatus(String plateNumber, UpdateVehicleStatusRequest req) {
        Vehicle v = findOrThrow(plateNumber);
        VehicleStatus old = v.getStatus();
        v.setStatus(req.getStatus());
        vehicleRepository.save(v);

        if (req.getStatus() == VehicleStatus.STOLEN) {
            VehicleOwnership ownership = ownershipRepository.findByPlateNumberAndStatus(plateNumber, OwnershipStatus.ACTIVE).orElse(null);
            InsuranceRecord ins = insuranceRepository.findByPlateNumberAndStatus(plateNumber, InsuranceStatus.ACTIVE).orElse(null);
            TechnicalInspection insp = inspectionRepository.findFirstByPlateNumberOrderByInspectionDateDesc(plateNumber).orElse(null);
            bridgePublisherService.publishVehicleRecord(v, ownership != null ? ownership.getOwnerNationalId() : null, ins, insp);
            if (ownership != null && ownership.getOwnerNationalId() != null) {
                notificationClient.send(ownership.getOwnerNationalId(), "VEHICLE_REPORTED_STOLEN", "EN",
                        Map.of("plateNumber", plateNumber));
            }
        }
        return toResponse(v);
    }

    private Vehicle findOrThrow(String plateNumber) {
        return vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + plateNumber));
    }

    private VehicleResponse toResponse(Vehicle v) {
        return new VehicleResponse(v.getVehicleId(), v.getPlateNumber(), v.getVin(), v.getMake(),
                v.getModel(), v.getYear(), v.getColor(), v.getEngineVolume(),
                v.getFuelType().name(), v.getVehicleType().name(), v.getRegisteredAt(), v.getStatus().name());
    }
}
