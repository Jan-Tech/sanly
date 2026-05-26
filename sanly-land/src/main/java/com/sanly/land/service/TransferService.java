package com.sanly.land.service;

import com.sanly.land.client.BridgePublisherService;
import com.sanly.land.client.BridgeQueryService;
import com.sanly.land.client.CitizenRegistryClient;
import com.sanly.land.client.NotificationClient;
import com.sanly.land.config.UserDetailsImpl;
import com.sanly.land.dto.request.SubmitTransferRequest;
import com.sanly.land.dto.response.TransferApplicationResponse;
import com.sanly.land.entity.*;
import com.sanly.land.exception.InvalidOperationException;
import com.sanly.land.exception.RecordNotFoundException;
import com.sanly.land.repository.OwnershipRepository;
import com.sanly.land.repository.PropertyRepository;
import com.sanly.land.repository.TransferApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferApplicationRepository transferRepository;
    private final PropertyRepository propertyRepository;
    private final OwnershipRepository ownershipRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgeQueryService bridgeQueryService;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;

    @Transactional
    public TransferApplicationResponse submit(SubmitTransferRequest req) {
        // Verify both parties exist
        citizenRegistryClient.verify(req.fromNationalId());
        citizenRegistryClient.verify(req.toNationalId());

        // Guard: property must be REGISTERED (not already locked)
        Property property = propertyRepository.findByCadastralNumber(req.cadastralNumber())
                .orElseThrow(() -> new RecordNotFoundException("Property not found: " + req.cadastralNumber()));
        if (property.getStatus() == PropertyStatus.UNDER_TRANSFER) {
            throw new InvalidOperationException("Property is already under transfer");
        }
        if (property.getStatus() == PropertyStatus.DISPUTED) {
            throw new InvalidOperationException("Property is disputed and cannot be transferred");
        }

        // Lock property during transfer
        property.setStatus(PropertyStatus.UNDER_TRANSFER);
        propertyRepository.save(property);

        TransferApplication app = TransferApplication.builder()
                .cadastralNumber(req.cadastralNumber())
                .fromNationalId(req.fromNationalId())
                .toNationalId(req.toNationalId())
                .transferType(req.transferType())
                .agreedPrice(req.agreedPrice())
                .applicationDate(req.applicationDate())
                .notes(req.notes())
                .status(TransferStatus.PENDING)
                .build();
        TransferApplication saved = transferRepository.save(app);

        // Notify seller
        notificationClient.send(req.fromNationalId(), "PROPERTY_TRANSFER_INITIATED", "EN",
                Map.of(
                        "cadastralNumber", req.cadastralNumber(),
                        "address",        property.getAddress() != null ? property.getAddress() : req.cadastralNumber(),
                        "transferType",   req.transferType().name()
                ));

        return TransferApplicationResponse.from(saved);
    }

    public TransferApplicationResponse findById(UUID id) {
        return TransferApplicationResponse.from(getOrThrow(id));
    }

    public List<TransferApplicationResponse> findByProperty(String cadastralNumber) {
        return transferRepository.findByCadastralNumber(cadastralNumber)
                .stream().map(TransferApplicationResponse::from).toList();
    }

    public List<TransferApplicationResponse> findByCitizen(String nationalId) {
        return transferRepository.findByCitizenNationalId(nationalId)
                .stream().map(TransferApplicationResponse::from).toList();
    }

    public List<TransferApplicationResponse> findPending() {
        return transferRepository.findByStatus(TransferStatus.PENDING)
                .stream().map(TransferApplicationResponse::from).toList();
    }

    @Transactional
    public TransferApplicationResponse approve(UUID id, Authentication auth) {
        // Pessimistic lock on the application to prevent double-approval
        TransferApplication app = transferRepository.findByIdWithLock(id)
                .orElseThrow(() -> new RecordNotFoundException("Transfer application not found: " + id));

        if (app.getStatus() != TransferStatus.PENDING) {
            throw new InvalidOperationException("Application is not pending: " + app.getStatus());
        }

        // Tax compliance check via bridge
        if (bridgeQueryService.isSellerTaxNonCompliant(app.getFromNationalId())) {
            throw new InvalidOperationException(
                    "Transfer cannot be approved: seller has outstanding tax obligations. " +
                    "Resolve tax status before transferring property.");
        }

        // Pessimistic lock on the property
        Property property = propertyRepository.findByCadastralNumberWithLock(app.getCadastralNumber())
                .orElseThrow(() -> new RecordNotFoundException("Property not found: " + app.getCadastralNumber()));

        // Atomically: mark old ownerships TRANSFERRED
        List<Ownership> activeOwnerships = ownershipRepository
                .findByCadastralNumberAndStatus(app.getCadastralNumber(), OwnershipStatus.ACTIVE);
        for (Ownership o : activeOwnerships) {
            o.setStatus(OwnershipStatus.TRANSFERRED);
            ownershipRepository.save(o);
        }

        // Create new ownership for the receiver
        int totalShare = activeOwnerships.stream().mapToInt(Ownership::getOwnershipShare).sum();
        Ownership newOwnership = Ownership.builder()
                .cadastralNumber(app.getCadastralNumber())
                .ownerNationalId(app.getToNationalId())
                .ownershipShare(totalShare > 0 ? totalShare : 100)
                .ownershipType(OwnershipType.SOLE)
                .acquiredAt(LocalDate.now())
                .acquiredVia(toAcquiredVia(app.getTransferType()))
                .status(OwnershipStatus.ACTIVE)
                .build();
        Ownership savedOwnership = ownershipRepository.save(newOwnership);

        // Restore property to REGISTERED
        property.setStatus(PropertyStatus.REGISTERED);
        propertyRepository.save(property);

        UUID officerId = auth != null ? ((UserDetailsImpl) auth.getPrincipal()).getOfficerId() : null;
        app.setStatus(TransferStatus.APPROVED);
        app.setProcessedByOfficerId(officerId);
        app.setProcessedAt(LocalDateTime.now());
        TransferApplication saved = transferRepository.save(app);

        // Async bridge publish
        bridgePublisherService.publishOwnership(savedOwnership.getOwnershipId());

        // Notify both parties
        Map<String, String> meta = Map.of(
                "cadastralNumber", app.getCadastralNumber(),
                "newOwnerName",    app.getToNationalId(),
                "transferDate",    LocalDate.now().toString()
        );
        notificationClient.send(app.getFromNationalId(), "PROPERTY_TRANSFER_APPROVED", "EN", meta);
        notificationClient.send(app.getToNationalId(),   "PROPERTY_TRANSFER_APPROVED", "EN", meta);

        return TransferApplicationResponse.from(saved);
    }

    @Transactional
    public TransferApplicationResponse reject(UUID id, String reason, Authentication auth) {
        TransferApplication app = getOrThrow(id);
        if (app.getStatus() != TransferStatus.PENDING) {
            throw new InvalidOperationException("Application is not pending: " + app.getStatus());
        }

        // Restore property status
        propertyRepository.findByCadastralNumber(app.getCadastralNumber()).ifPresent(p -> {
            p.setStatus(PropertyStatus.REGISTERED);
            propertyRepository.save(p);
        });

        UUID officerId = auth != null ? ((UserDetailsImpl) auth.getPrincipal()).getOfficerId() : null;
        app.setStatus(TransferStatus.REJECTED);
        app.setRejectionReason(reason);
        app.setProcessedByOfficerId(officerId);
        app.setProcessedAt(LocalDateTime.now());
        TransferApplication saved = transferRepository.save(app);

        notificationClient.send(app.getFromNationalId(), "PROPERTY_TRANSFER_REJECTED", "EN",
                Map.of(
                        "cadastralNumber", app.getCadastralNumber(),
                        "rejectionReason", reason != null ? reason : "Not specified"
                ));

        return TransferApplicationResponse.from(saved);
    }

    private TransferApplication getOrThrow(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Transfer application not found: " + id));
    }

    private AcquiredVia toAcquiredVia(TransferType t) {
        return switch (t) {
            case SALE        -> AcquiredVia.PURCHASE;
            case GIFT        -> AcquiredVia.GIFT;
            case INHERITANCE -> AcquiredVia.INHERITANCE;
            case COURT_ORDER -> AcquiredVia.COURT_ORDER;
        };
    }
}
