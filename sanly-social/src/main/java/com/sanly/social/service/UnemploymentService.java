package com.sanly.social.service;

import com.sanly.social.client.CitizenRegistryClient;
import com.sanly.social.client.NotificationClient;
import com.sanly.social.dto.request.AutoTriggerRequest;
import com.sanly.social.dto.request.RegisterUnemploymentRequest;
import com.sanly.social.dto.response.UnemploymentResponse;
import com.sanly.social.entity.BenefitType;
import com.sanly.social.entity.UnemploymentRecord;
import com.sanly.social.entity.UnemploymentStatus;
import com.sanly.social.exception.DuplicateResourceException;
import com.sanly.social.exception.InvalidOperationException;
import com.sanly.social.exception.RecordNotFoundException;
import com.sanly.social.repository.UnemploymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnemploymentService {

    private final UnemploymentRecordRepository unemploymentRepo;
    private final CitizenRegistryClient citizenClient;
    private final BenefitClaimService claimService;
    private final NotificationClient notificationClient;

    @Transactional
    public UnemploymentResponse register(RegisterUnemploymentRequest req) {
        citizenClient.verify(req.citizenNationalId());

        var existing = unemploymentRepo.findByCitizenNationalId(req.citizenNationalId());
        if (existing.isPresent() && existing.get().getStatus() == UnemploymentStatus.REGISTERED)
            throw new DuplicateResourceException("Already registered as unemployed: " + req.citizenNationalId());

        UnemploymentRecord record = UnemploymentRecord.builder()
                .citizenNationalId(req.citizenNationalId())
                .lastEmployer(req.lastEmployer())
                .lastEmploymentDate(req.lastEmploymentDate())
                .reason(req.reason())
                .build();
        unemploymentRepo.save(record);

        claimService.autoTrigger(new AutoTriggerRequest(
                req.citizenNationalId(), BenefitType.UNEMPLOYMENT, null, "Registered as unemployed"));

        return UnemploymentResponse.from(record);
    }

    public UnemploymentResponse findByNationalId(String nationalId) {
        return unemploymentRepo.findByCitizenNationalId(nationalId)
                .map(UnemploymentResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("No unemployment record for: " + nationalId));
    }

    @Transactional
    public UnemploymentResponse markEmployed(String nationalId) {
        UnemploymentRecord record = unemploymentRepo.findByCitizenNationalId(nationalId)
                .orElseThrow(() -> new RecordNotFoundException("No unemployment record for: " + nationalId));
        if (record.getStatus() != UnemploymentStatus.REGISTERED)
            throw new InvalidOperationException("Record is not REGISTERED: " + nationalId);

        record.setStatus(UnemploymentStatus.EMPLOYED);
        unemploymentRepo.save(record);

        claimService.findByCitizen(nationalId).stream()
                .filter(c -> c.status() == com.sanly.social.entity.ClaimStatus.ACTIVE)
                .forEach(c -> {
                    try { claimService.suspend(c.claimCode()); } catch (Exception ignored) {}
                });

        notificationClient.send(nationalId, "UNEMPLOYMENT_BENEFIT_EXPIRED", "TK",
                Map.of("reason", "Citizen marked as employed"));
        return UnemploymentResponse.from(record);
    }
}
