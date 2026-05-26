package com.sanly.vehicle.service;

import com.sanly.vehicle.client.BridgePublisherService;
import com.sanly.vehicle.client.BridgeQueryService;
import com.sanly.vehicle.client.CitizenRegistryClient;
import com.sanly.vehicle.client.NotificationClient;
import com.sanly.vehicle.config.UserDetailsImpl;
import com.sanly.vehicle.dto.request.CreateTransferRequest;
import com.sanly.vehicle.dto.request.RejectTransferRequest;
import com.sanly.vehicle.dto.response.TransferApplicationResponse;
import com.sanly.vehicle.entity.*;
import com.sanly.vehicle.exception.BusinessException;
import com.sanly.vehicle.exception.ResourceNotFoundException;
import com.sanly.vehicle.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferApplicationRepository transferRepo;
    private final VehicleRepository vehicleRepo;
    private final VehicleOwnershipRepository ownershipRepo;
    private final InsuranceRecordRepository insuranceRepo;
    private final TechnicalInspectionRepository inspectionRepo;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgeQueryService bridgeQueryService;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;

    @Transactional
    public TransferApplicationResponse submit(CreateTransferRequest req) {
        if (req.getToNationalId() == null && req.getToBusinessNumber() == null)
            throw new BusinessException("Transfer recipient (toNationalId or toBusinessNumber) is required");

        Vehicle vehicle = vehicleRepo.findByPlateNumber(req.getPlateNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + req.getPlateNumber()));
        if (vehicle.getStatus() == VehicleStatus.STOLEN || vehicle.getStatus() == VehicleStatus.DEREGISTERED)
            throw new BusinessException("Cannot transfer vehicle with status: " + vehicle.getStatus());
        if (transferRepo.existsByPlateNumberAndStatus(req.getPlateNumber(), TransferStatus.PENDING))
            throw new BusinessException("A pending transfer already exists for this vehicle");

        citizenRegistryClient.verify(req.getFromNationalId());
        if (req.getToNationalId() != null) citizenRegistryClient.verify(req.getToNationalId());

        vehicle.setStatus(VehicleStatus.UNDER_TRANSFER);
        vehicleRepo.save(vehicle);

        TransferApplication app = transferRepo.save(TransferApplication.builder()
                .plateNumber(req.getPlateNumber())
                .fromNationalId(req.getFromNationalId())
                .toNationalId(req.getToNationalId())
                .toBusinessNumber(req.getToBusinessNumber())
                .transferType(req.getTransferType())
                .agreedPrice(req.getAgreedPrice())
                .applicationDate(LocalDate.now())
                .status(TransferStatus.PENDING)
                .build());
        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public TransferApplicationResponse findById(Long applicationId) {
        return toResponse(findOrThrow(applicationId));
    }

    @Transactional(readOnly = true)
    public List<TransferApplicationResponse> findByPlate(String plateNumber) {
        return transferRepo.findByPlateNumberOrderByApplicationDateDesc(plateNumber)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransferApplicationResponse> findByStatus(TransferStatus status) {
        return transferRepo.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public TransferApplicationResponse approve(Long applicationId) {
        TransferApplication app = findOrThrow(applicationId);
        if (app.getStatus() != TransferStatus.PENDING)
            throw new BusinessException("Transfer is not in PENDING status");

        if (!bridgeQueryService.isTaxCompliant(app.getFromNationalId()))
            throw new BusinessException("Seller is not tax-compliant — transfer blocked");

        VehicleOwnership current = ownershipRepo.findByPlateNumberAndStatus(app.getPlateNumber(), OwnershipStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("No active ownership record found"));
        current.setStatus(OwnershipStatus.TRANSFERRED);
        current.setOwnershipEndDate(LocalDate.now());
        ownershipRepo.save(current);

        ownershipRepo.save(VehicleOwnership.builder()
                .plateNumber(app.getPlateNumber())
                .ownerNationalId(app.getToNationalId())
                .ownerBusinessNumber(app.getToBusinessNumber())
                .ownershipStartDate(LocalDate.now())
                .acquiredVia(mapAcquiredVia(app.getTransferType()))
                .status(OwnershipStatus.ACTIVE)
                .build());

        Vehicle vehicle = vehicleRepo.findByPlateNumber(app.getPlateNumber()).orElseThrow();
        vehicle.setStatus(VehicleStatus.REGISTERED);
        vehicleRepo.save(vehicle);

        app.setStatus(TransferStatus.APPROVED);
        app.setProcessedByOfficerId(authenticatedOfficerId());
        app.setProcessedAt(LocalDateTime.now());
        transferRepo.save(app);

        InsuranceRecord ins = insuranceRepo.findByPlateNumberAndStatus(app.getPlateNumber(), InsuranceStatus.ACTIVE).orElse(null);
        TechnicalInspection insp = inspectionRepo.findFirstByPlateNumberOrderByInspectionDateDesc(app.getPlateNumber()).orElse(null);
        bridgePublisherService.publishVehicleRecord(vehicle, app.getToNationalId(), ins, insp);

        String targetNin = app.getToNationalId();
        if (targetNin != null) {
            notificationClient.send(targetNin, "VEHICLE_TRANSFER_APPROVED", "EN",
                    Map.of("plateNumber", app.getPlateNumber()));
        }
        notificationClient.send(app.getFromNationalId(), "VEHICLE_TRANSFER_APPROVED", "EN",
                Map.of("plateNumber", app.getPlateNumber()));

        return toResponse(app);
    }

    @Transactional
    public TransferApplicationResponse reject(Long applicationId, RejectTransferRequest req) {
        TransferApplication app = findOrThrow(applicationId);
        if (app.getStatus() != TransferStatus.PENDING)
            throw new BusinessException("Transfer is not in PENDING status");

        Vehicle vehicle = vehicleRepo.findByPlateNumber(app.getPlateNumber()).orElseThrow();
        vehicle.setStatus(VehicleStatus.REGISTERED);
        vehicleRepo.save(vehicle);

        app.setStatus(TransferStatus.REJECTED);
        app.setRejectionReason(req.getRejectionReason());
        app.setProcessedByOfficerId(authenticatedOfficerId());
        app.setProcessedAt(LocalDateTime.now());
        transferRepo.save(app);

        notificationClient.send(app.getFromNationalId(), "VEHICLE_TRANSFER_REJECTED", "EN",
                Map.of("plateNumber", app.getPlateNumber(), "reason", req.getRejectionReason()));
        return toResponse(app);
    }

    private AcquiredVia mapAcquiredVia(TransferType type) {
        return switch (type) {
            case SALE -> AcquiredVia.PURCHASE;
            case GIFT -> AcquiredVia.GIFT;
            case INHERITANCE -> AcquiredVia.INHERITANCE;
            case COURT_ORDER -> AcquiredVia.COURT_ORDER;
        };
    }

    private TransferApplication findOrThrow(Long id) {
        return transferRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer application not found: " + id));
    }

    private Long authenticatedOfficerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getOfficerId();
    }

    private TransferApplicationResponse toResponse(TransferApplication a) {
        return new TransferApplicationResponse(a.getApplicationId(), a.getPlateNumber(),
                a.getFromNationalId(), a.getToNationalId(), a.getToBusinessNumber(),
                a.getTransferType().name(), a.getAgreedPrice(), a.getApplicationDate(),
                a.getStatus().name(), a.getProcessedByOfficerId(), a.getProcessedAt(), a.getRejectionReason());
    }
}
