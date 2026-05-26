package com.sanly.customs.service;

import com.sanly.customs.client.BridgePublisherService;
import com.sanly.customs.client.BridgeQueryService;
import com.sanly.customs.client.CitizenRegistryClient;
import com.sanly.customs.client.NotificationClient;
import com.sanly.customs.config.UserDetailsImpl;
import com.sanly.customs.dto.request.CreateDeclarationRequest;
import com.sanly.customs.dto.request.RecordPaymentRequest;
import com.sanly.customs.dto.request.RejectDeclarationRequest;
import com.sanly.customs.dto.response.DeclarationResponse;
import com.sanly.customs.entity.*;
import com.sanly.customs.exception.InvalidOperationException;
import com.sanly.customs.exception.RecordNotFoundException;
import com.sanly.customs.repository.CustomsDeclarationRepository;
import com.sanly.customs.repository.CustomsPortRepository;
import com.sanly.customs.repository.DeclarationSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeclarationService {

    private final CustomsDeclarationRepository declarationRepo;
    private final DeclarationSequenceRepository seqRepo;
    private final CustomsPortRepository portRepo;
    private final CitizenRegistryClient citizenClient;
    private final BridgeQueryService bridgeQuery;
    private final BridgePublisherService bridgePublisher;
    private final NotificationClient notificationClient;

    @Transactional
    public DeclarationResponse create(CreateDeclarationRequest req) {
        // Validate declarant
        if (req.declarantType() == DeclarantType.CITIZEN) {
            if (req.declarantNationalId() == null || req.declarantNationalId().isBlank())
                throw new InvalidOperationException("Citizen NIN is required for CITIZEN declarant type");
            citizenClient.verify(req.declarantNationalId());
        } else {
            if (req.declarantBusinessNumber() == null || req.declarantBusinessNumber().isBlank())
                throw new InvalidOperationException("Business number is required for BUSINESS declarant type");
            if (!bridgeQuery.isBusinessActive(req.declarantBusinessNumber()))
                throw new InvalidOperationException("Business " + req.declarantBusinessNumber() +
                        " is not active or not found in registry");
        }

        // Validate port exists
        portRepo.findByPortCode(req.portCode())
                .orElseThrow(() -> new RecordNotFoundException("Port not found: " + req.portCode()));

        CustomsDeclaration decl = CustomsDeclaration.builder()
                .declarationCode(generateCode())
                .declarantType(req.declarantType())
                .declarantNationalId(req.declarantNationalId())
                .declarantBusinessNumber(req.declarantBusinessNumber())
                .declarationType(req.declarationType())
                .portCode(req.portCode())
                .cargoDescription(req.cargoDescription())
                .hsCode(req.hsCode())
                .countryOfOrigin(req.countryOfOrigin())
                .countryOfDestination(req.countryOfDestination())
                .quantity(req.quantity())
                .unit(req.unit())
                .declaredValue(req.declaredValue())
                .currency(req.currency() != null ? req.currency() : "TMT")
                .notes(req.notes())
                .status(DeclarationStatus.DRAFT)
                .build();
        return DeclarationResponse.from(declarationRepo.save(decl));
    }

    @Transactional
    public DeclarationResponse submit(String declarationCode) {
        CustomsDeclaration decl = declarationRepo.findByDeclarationCodeWithLock(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));
        if (decl.getStatus() != DeclarationStatus.DRAFT)
            throw new InvalidOperationException("Only DRAFT declarations can be submitted");
        decl.setStatus(DeclarationStatus.SUBMITTED);
        declarationRepo.save(decl);

        String portName = portRepo.findByPortCode(decl.getPortCode())
                .map(p -> p.getName()).orElse(decl.getPortCode());
        String recipientId = getDeclarantId(decl);
        if (recipientId != null) {
            notificationClient.send(recipientId, "CUSTOMS_DECLARATION_SUBMITTED", "TK",
                    Map.of("declarationCode", declarationCode, "portName", portName,
                           "dutiesOwed", decl.getDutiesOwed() != null ? decl.getDutiesOwed() : "TBD"));
        }
        return DeclarationResponse.from(decl);
    }

    @Transactional
    public DeclarationResponse clear(String declarationCode) {
        CustomsDeclaration decl = declarationRepo.findByDeclarationCodeWithLock(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));

        if (decl.getStatus() != DeclarationStatus.SUBMITTED && decl.getStatus() != DeclarationStatus.UNDER_REVIEW)
            throw new InvalidOperationException("Declaration must be SUBMITTED or UNDER_REVIEW to clear");

        // Tax compliance check
        String subjectId = getDeclarantId(decl);
        if (subjectId != null && bridgeQuery.isTaxNonCompliant(subjectId))
            throw new InvalidOperationException(
                    "Clearance blocked: declarant has outstanding tax obligations");

        // Duties must be settled
        BigDecimal owed = parseSafe(decl.getDutiesOwed());
        BigDecimal paid = parseSafe(decl.getDutiesPaid());
        if (owed.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(owed) < 0)
            throw new InvalidOperationException(
                    "Clearance blocked: outstanding duties of " + owed.subtract(paid).toPlainString() + " remain unpaid");

        decl.setStatus(DeclarationStatus.CLEARED);
        decl.setProcessedByOfficerId(getOfficerId());
        decl.setProcessedAt(LocalDateTime.now());
        declarationRepo.save(decl);

        bridgePublisher.publishClearance(decl);

        if (subjectId != null) {
            notificationClient.send(subjectId, "CUSTOMS_DECLARATION_CLEARED", "TK",
                    Map.of("declarationCode", declarationCode));
        }
        return DeclarationResponse.from(decl);
    }

    @Transactional
    public DeclarationResponse reject(String declarationCode, RejectDeclarationRequest req) {
        CustomsDeclaration decl = declarationRepo.findByDeclarationCodeWithLock(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));
        if (decl.getStatus() == DeclarationStatus.CLEARED || decl.getStatus() == DeclarationStatus.REJECTED)
            throw new InvalidOperationException("Cannot reject a " + decl.getStatus() + " declaration");

        decl.setStatus(DeclarationStatus.REJECTED);
        decl.setRejectionReason(req.rejectionReason());
        decl.setProcessedByOfficerId(getOfficerId());
        decl.setProcessedAt(LocalDateTime.now());
        declarationRepo.save(decl);

        String recipientId = getDeclarantId(decl);
        if (recipientId != null) {
            notificationClient.send(recipientId, "CUSTOMS_DECLARATION_REJECTED", "TK",
                    Map.of("declarationCode", declarationCode,
                           "reason", req.rejectionReason() != null ? req.rejectionReason() : ""));
        }
        return DeclarationResponse.from(decl);
    }

    @Transactional
    public DeclarationResponse hold(String declarationCode) {
        CustomsDeclaration decl = declarationRepo.findByDeclarationCodeWithLock(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));
        if (decl.getStatus() != DeclarationStatus.SUBMITTED && decl.getStatus() != DeclarationStatus.UNDER_REVIEW)
            throw new InvalidOperationException("Only SUBMITTED or UNDER_REVIEW declarations can be held");

        decl.setStatus(DeclarationStatus.HELD);
        declarationRepo.save(decl);

        String portName = portRepo.findByPortCode(decl.getPortCode())
                .map(p -> p.getName()).orElse(decl.getPortCode());
        String recipientId = getDeclarantId(decl);
        if (recipientId != null) {
            notificationClient.send(recipientId, "CUSTOMS_DECLARATION_HELD", "TK",
                    Map.of("declarationCode", declarationCode, "portName", portName));
        }
        return DeclarationResponse.from(decl);
    }

    @Transactional
    public DeclarationResponse recordPayment(String declarationCode, RecordPaymentRequest req) {
        CustomsDeclaration decl = declarationRepo.findByDeclarationCodeWithLock(declarationCode)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));

        BigDecimal current = parseSafe(decl.getDutiesPaid());
        BigDecimal added   = parseSafe(req.amountPaid());
        BigDecimal total   = current.add(added);
        decl.setDutiesPaid(total.toPlainString());

        // Auto-advance to UNDER_REVIEW when full payment received
        BigDecimal owed = parseSafe(decl.getDutiesOwed());
        if (owed.compareTo(BigDecimal.ZERO) > 0 && total.compareTo(owed) >= 0
                && decl.getStatus() == DeclarationStatus.SUBMITTED) {
            decl.setStatus(DeclarationStatus.UNDER_REVIEW);
        }
        return DeclarationResponse.from(declarationRepo.save(decl));
    }

    public DeclarationResponse findByCode(String declarationCode) {
        return declarationRepo.findByDeclarationCode(declarationCode)
                .map(DeclarationResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Declaration not found: " + declarationCode));
    }

    public List<DeclarationResponse> findByDeclarant(String id) {
        List<CustomsDeclaration> byNin = declarationRepo.findByDeclarantNationalId(id);
        List<CustomsDeclaration> byBiz = declarationRepo.findByDeclarantBusinessNumber(id);
        return java.util.stream.Stream.concat(byNin.stream(), byBiz.stream())
                .map(DeclarationResponse::from).toList();
    }

    public List<DeclarationResponse> findByPort(String portCode, DeclarationStatus status) {
        if (status != null)
            return declarationRepo.findByPortCodeAndStatus(portCode, status)
                    .stream().map(DeclarationResponse::from).toList();
        return declarationRepo.findByPortCode(portCode)
                .stream().map(DeclarationResponse::from).toList();
    }

    // Public verify — no auth
    public DeclarationResponse verify(String declarationCode) {
        return findByCode(declarationCode);
    }

    protected String generateCode() {
        int year = LocalDate.now().getYear();
        seqRepo.insertIfNotExists(year);
        DeclarationSequence seq = seqRepo.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Sequence not found for year " + year));
        int next = seq.getNextValue();
        seq.setNextValue(next + 1);
        seqRepo.save(seq);
        return "TM-CUS-%d%06d".formatted(year, next);
    }

    private String getDeclarantId(CustomsDeclaration decl) {
        return decl.getDeclarantNationalId() != null
                ? decl.getDeclarantNationalId()
                : decl.getDeclarantBusinessNumber();
    }

    private BigDecimal parseSafe(String value) {
        try { return new BigDecimal(value == null ? "0" : value); }
        catch (NumberFormatException e) { return BigDecimal.ZERO; }
    }

    private UUID getOfficerId() {
        try {
            Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (p instanceof UserDetailsImpl ud) return ud.getOfficerId();
        } catch (Exception ignored) {}
        return null;
    }
}
