package com.sanly.land.service;

import com.sanly.land.client.NotificationClient;
import com.sanly.land.dto.request.AssignOwnershipRequest;
import com.sanly.land.dto.response.OwnershipResponse;
import com.sanly.land.entity.*;
import com.sanly.land.exception.RecordNotFoundException;
import com.sanly.land.repository.OwnershipRepository;
import com.sanly.land.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnershipService {
    private final OwnershipRepository ownershipRepository;
    private final PropertyRepository propertyRepository;
    private final NotificationClient notificationClient;

    @Transactional
    public OwnershipResponse assign(AssignOwnershipRequest req) {
        if (!propertyRepository.existsByCadastralNumber(req.cadastralNumber())) {
            throw new RecordNotFoundException("Property not found: " + req.cadastralNumber());
        }
        Ownership ownership = Ownership.builder()
                .cadastralNumber(req.cadastralNumber())
                .ownerNationalId(req.ownerNationalId())
                .ownershipShare(req.ownershipShare())
                .ownershipType(req.ownershipType())
                .acquiredAt(req.acquiredAt())
                .acquiredVia(req.acquiredVia())
                .status(OwnershipStatus.ACTIVE)
                .build();
        return OwnershipResponse.from(ownershipRepository.save(ownership));
    }

    public List<OwnershipResponse> findByProperty(String cadastralNumber) {
        return ownershipRepository.findByCadastralNumberAndStatus(cadastralNumber, OwnershipStatus.ACTIVE)
                .stream().map(OwnershipResponse::from).toList();
    }

    public List<OwnershipResponse> findByCitizen(String nationalId) {
        return ownershipRepository.findByOwnerNationalIdAndStatus(nationalId, OwnershipStatus.ACTIVE)
                .stream().map(OwnershipResponse::from).toList();
    }

    @Transactional
    public void handleDeceased(String deceasedNationalId) {
        List<Ownership> active = ownershipRepository
                .findByOwnerNationalIdAndStatus(deceasedNationalId, OwnershipStatus.ACTIVE);

        if (active.isEmpty()) {
            log.info("No active ownerships found for deceased {}", deceasedNationalId);
            return;
        }

        for (Ownership o : active) {
            o.setStatus(OwnershipStatus.INHERITED);
            ownershipRepository.save(o);

            // Set property to UNDER_TRANSFER
            propertyRepository.findByCadastralNumber(o.getCadastralNumber()).ifPresent(p -> {
                p.setStatus(PropertyStatus.UNDER_TRANSFER);
                propertyRepository.save(p);
            });

            // Notify co-owners
            List<Ownership> coOwners = ownershipRepository
                    .findByCadastralNumberAndStatus(o.getCadastralNumber(), OwnershipStatus.ACTIVE)
                    .stream().filter(co -> !co.getOwnerNationalId().equals(deceasedNationalId)).toList();

            for (Ownership coOwner : coOwners) {
                notificationClient.send(coOwner.getOwnerNationalId(), "PROPERTY_INHERITANCE_PENDING", "EN",
                        Map.of(
                                "cadastralNumber", o.getCadastralNumber(),
                                "deceasedNin",     deceasedNationalId
                        ));
            }
        }
        log.info("Handled deceased ownership for {}: {} properties set to UNDER_TRANSFER",
                deceasedNationalId, active.size());
    }
}
