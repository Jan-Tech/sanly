package com.sanly.medical.service;

import com.sanly.medical.client.BridgePublisherService;
import com.sanly.medical.client.CitizenRegistryClient;
import com.sanly.medical.client.NotificationClient;
import com.sanly.medical.dto.request.DispensePrescriptionRequest;
import com.sanly.medical.dto.request.IssuePrescriptionRequest;
import com.sanly.medical.dto.response.DispensingResponse;
import com.sanly.medical.dto.response.PrescriptionResponse;
import com.sanly.medical.entity.*;
import com.sanly.medical.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository          prescriptionRepository;
    private final PrescriptionDispensingRepository dispensingRepository;
    private final PrescriptionSequenceRepository   sequenceRepository;
    private final CitizenRegistryClient            citizenRegistryClient;
    private final BridgePublisherService           bridgePublisher;
    private final NotificationClient               notificationClient;
    private final ClinicService                    clinicService;
    private final RestTemplate                     restTemplate;

    @Value("${sanly.bridge.base-url}")
    private String bridgeBaseUrl;
    @Value("${sanly.bridge.institution-code}")
    private String institutionCode;
    @Value("${sanly.bridge.institution-key}")
    private String institutionKey;

    // ── Issue ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PrescriptionResponse issue(IssuePrescriptionRequest req, Long doctorId, Long clinicId) {
        citizenRegistryClient.verify(req.getCitizenNationalId());

        int year = LocalDate.now().getYear();
        String code = generateCode(year);

        LocalDate expiresAt = req.getExpiresAt() != null
                ? req.getExpiresAt()
                : LocalDate.now().plusDays(30);

        Prescription rx = Prescription.builder()
                .prescriptionCode(code)
                .citizenNationalId(req.getCitizenNationalId())
                .issuedByDoctorId(doctorId)
                .clinicId(clinicId)
                .diagnosisCode(req.getDiagnosisCode())
                .medicationName(req.getMedicationName())
                .medicationDosage(req.getMedicationDosage())
                .medicationForm(req.getMedicationForm())
                .quantity(req.getQuantity())
                .unit(req.getUnit())
                .refillsAllowed(req.getRefillsAllowed())
                .refillsUsed(0)
                .instructions(req.getInstructions())
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .status(PrescriptionStatus.ACTIVE)
                .build();

        Prescription saved = prescriptionRepository.save(rx);

        publishPrescriptionToBridge(saved);

        String clinicName;
        try { clinicName = clinicService.getById(clinicId).getName(); }
        catch (Exception e) { clinicName = "Clinic #" + clinicId; }
        notificationClient.send(req.getCitizenNationalId(), "PRESCRIPTION_ISSUED", "EN",
                Map.of(
                        "prescriptionCode", code,
                        "medicationName",   req.getMedicationName(),
                        "dosage",           req.getMedicationDosage(),
                        "clinicName",       clinicName,
                        "doctorName",       "Doctor #" + doctorId,
                        "expiresAt",        expiresAt.toString()
                ));

        log.info("Prescription {} issued for NIN={} by doctor={}", code,
                req.getCitizenNationalId(), doctorId);
        return PrescriptionResponse.from(saved);
    }

    // ── Get ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getByCode(String prescriptionCode) {
        return prescriptionRepository.findByPrescriptionCode(prescriptionCode)
                .map(PrescriptionResponse::from)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getByCitizen(String nationalId) {
        return prescriptionRepository
                .findByCitizenNationalIdOrderByIssuedAtDesc(nationalId)
                .stream().map(PrescriptionResponse::from).toList();
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PrescriptionResponse cancel(String prescriptionCode, Long requestingDoctorId) {
        Prescription rx = prescriptionRepository.findByPrescriptionCode(prescriptionCode)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionCode));

        if (!rx.getIssuedByDoctorId().equals(requestingDoctorId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the issuing doctor can cancel this prescription");
        }
        if (rx.getStatus() != PrescriptionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot cancel prescription with status: " + rx.getStatus());
        }

        rx.setStatus(PrescriptionStatus.CANCELLED);
        Prescription saved = prescriptionRepository.save(rx);
        publishPrescriptionToBridge(saved);

        log.info("Prescription {} cancelled by doctor={}", prescriptionCode, requestingDoctorId);
        return PrescriptionResponse.from(saved);
    }

    // ── Dispense ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DispensingResponse dispense(String prescriptionCode, String pharmacyCode,
                                        DispensePrescriptionRequest req) {
        // Pessimistic lock to prevent concurrent double-dispensing
        Prescription rx = prescriptionRepository.findByPrescriptionCodeForUpdate(prescriptionCode)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + prescriptionCode));

        if (rx.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new IllegalStateException("Prescription has been cancelled");
        }
        if (rx.getStatus() == PrescriptionStatus.FULLY_DISPENSED) {
            throw new IllegalStateException("This prescription has been fully dispensed");
        }
        if (rx.getStatus() == PrescriptionStatus.EXPIRED
                || LocalDate.now().isAfter(rx.getExpiresAt())) {
            rx.setStatus(PrescriptionStatus.EXPIRED);
            prescriptionRepository.save(rx);
            throw new IllegalStateException("Prescription has expired");
        }
        int refillsRemaining = rx.getRefillsAllowed() - rx.getRefillsUsed();
        if (refillsRemaining < 0) {
            throw new IllegalStateException("No refills remaining");
        }

        // Record dispensing
        PrescriptionDispensing dispensing = PrescriptionDispensing.builder()
                .prescriptionCode(prescriptionCode)
                .dispensedByPharmacyCode(pharmacyCode)
                .dispensedAt(LocalDateTime.now())
                .quantityDispensed(req.getQuantityDispensed())
                .pharmacistNationalId(req.getPharmacistNationalId())
                .notes(req.getNotes())
                .build();
        dispensingRepository.save(dispensing);

        // Update prescription status
        rx.setRefillsUsed(rx.getRefillsUsed() + 1);
        if (rx.getRefillsUsed() >= rx.getRefillsAllowed()) {
            rx.setStatus(PrescriptionStatus.FULLY_DISPENSED);
        } else {
            rx.setStatus(PrescriptionStatus.PARTIALLY_DISPENSED);
        }
        Prescription updated = prescriptionRepository.save(rx);
        publishPrescriptionToBridge(updated);

        log.info("Prescription {} dispensed by pharmacy={} status={}",
                prescriptionCode, pharmacyCode, updated.getStatus());
        return DispensingResponse.from(dispensing);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    @Transactional
    protected String generateCode(int year) {
        PrescriptionSequence seq = sequenceRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    PrescriptionSequence s = new PrescriptionSequence();
                    s.setYear(year);
                    s.setNextValue(1L);
                    return s;
                });
        long val = seq.getNextValue();
        seq.setNextValue(val + 1);
        sequenceRepository.save(seq);
        return String.format("TM-RX-%d%06d", year, val);
    }

    private void publishPrescriptionToBridge(Prescription rx) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Institution-Code", institutionCode);
            headers.set("X-Institution-Key", institutionKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> summary = new HashMap<>();
            summary.put("prescriptionCode", rx.getPrescriptionCode());
            summary.put("medicationName",   rx.getMedicationName());
            summary.put("medicationDosage", rx.getMedicationDosage());
            summary.put("status",           rx.getStatus().name());
            summary.put("refillsAllowed",   rx.getRefillsAllowed());
            summary.put("refillsUsed",      rx.getRefillsUsed());
            summary.put("expiresAt",        rx.getExpiresAt().toString());

            Map<String, Object> body = Map.of(
                    "nationalId",  rx.getCitizenNationalId(),
                    "dataType",    "PRESCRIPTION",
                    "recordRef",   rx.getPrescriptionId().toString(),
                    "summary",     summary,
                    "expiresAt",   rx.getExpiresAt().atStartOfDay().toString()
            );

            restTemplate.postForEntity(
                    bridgeBaseUrl + "/api/v1/exchange/publish",
                    new HttpEntity<>(body, headers),
                    Void.class);

            rx.setBridgePublished(true);
            prescriptionRepository.save(rx);

        } catch (Exception ex) {
            log.warn("Bridge publish failed for prescription {}: {}", rx.getPrescriptionCode(), ex.getMessage());
        }
    }
}
