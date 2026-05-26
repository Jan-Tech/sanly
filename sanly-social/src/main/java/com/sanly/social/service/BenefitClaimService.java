package com.sanly.social.service;

import com.sanly.social.client.BridgePublisherService;
import com.sanly.social.client.BridgeQueryService;
import com.sanly.social.client.CitizenRegistryClient;
import com.sanly.social.client.NotificationClient;
import com.sanly.social.dto.request.ApproveClaimRequest;
import com.sanly.social.dto.request.AutoTriggerRequest;
import com.sanly.social.dto.request.RejectClaimRequest;
import com.sanly.social.dto.request.SubmitClaimRequest;
import com.sanly.social.dto.response.ClaimResponse;
import com.sanly.social.entity.*;
import com.sanly.social.exception.InvalidOperationException;
import com.sanly.social.exception.RecordNotFoundException;
import com.sanly.social.repository.BenefitClaimRepository;
import com.sanly.social.repository.BenefitProgramRepository;
import com.sanly.social.repository.ClaimSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitClaimService {

    private final BenefitClaimRepository claimRepo;
    private final BenefitProgramRepository programRepo;
    private final ClaimSequenceRepository seqRepo;
    private final CitizenRegistryClient citizenClient;
    private final BridgeQueryService bridgeQuery;
    private final BridgePublisherService bridgePublisher;
    private final NotificationClient notificationClient;
    private final BenefitPaymentService paymentService;

    @Transactional
    public ClaimResponse submit(SubmitClaimRequest req) {
        citizenClient.verify(req.citizenNationalId());

        BenefitProgram program = programRepo.findByProgramCode(req.programCode())
                .orElseThrow(() -> new RecordNotFoundException("Program not found: " + req.programCode()));
        if (program.getStatus() != ProgramStatus.ACTIVE)
            throw new InvalidOperationException("Program is not active: " + req.programCode());

        BenefitClaim claim = BenefitClaim.builder()
                .claimCode(generateClaimCode())
                .citizenNationalId(req.citizenNationalId())
                .programCode(req.programCode())
                .claimType(ClaimType.CITIZEN_APPLIED)
                .status(ClaimStatus.PENDING)
                .notes(req.notes())
                .build();
        return ClaimResponse.from(claimRepo.save(claim));
    }

    @Transactional
    public ClaimResponse approve(String claimCode, ApproveClaimRequest req) {
        BenefitClaim claim = claimRepo.findByClaimCodeWithLock(claimCode)
                .orElseThrow(() -> new RecordNotFoundException("Claim not found: " + claimCode));

        if (claim.getStatus() != ClaimStatus.PENDING)
            throw new InvalidOperationException("Claim is not in PENDING state: " + claimCode);

        if (bridgeQuery.isTaxNonCompliant(claim.getCitizenNationalId()))
            throw new InvalidOperationException("Claim cannot be approved: citizen has outstanding tax obligations.");

        BenefitProgram program = programRepo.findByProgramCode(claim.getProgramCode())
                .orElseThrow(() -> new RecordNotFoundException("Program not found: " + claim.getProgramCode()));

        UUID officerId = getOfficerId();
        claim.setStatus(ClaimStatus.ACTIVE);
        claim.setApprovedAt(LocalDateTime.now());
        claim.setApprovedByOfficerId(officerId);
        if (req != null && req.notes() != null) claim.setNotes(req.notes());

        if (program.getMaxDurationMonths() != null)
            claim.setExpiresAt(LocalDate.now().plusMonths(program.getMaxDurationMonths()));

        claimRepo.save(claim);
        paymentService.schedulePayment(claim, program);
        bridgePublisher.publishBenefitStatus(claim.getCitizenNationalId());

        notificationClient.send(claim.getCitizenNationalId(), "BENEFIT_CLAIM_APPROVED", "TK",
                Map.of("claimCode", claimCode, "programCode", claim.getProgramCode()));
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse reject(String claimCode, RejectClaimRequest req) {
        BenefitClaim claim = claimRepo.findByClaimCodeWithLock(claimCode)
                .orElseThrow(() -> new RecordNotFoundException("Claim not found: " + claimCode));

        if (claim.getStatus() != ClaimStatus.PENDING)
            throw new InvalidOperationException("Claim is not in PENDING state: " + claimCode);

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectionReason(req.rejectionReason());
        claimRepo.save(claim);

        notificationClient.send(claim.getCitizenNationalId(), "BENEFIT_CLAIM_REJECTED", "TK",
                Map.of("claimCode", claimCode, "reason", req.rejectionReason()));
        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse suspend(String claimCode) {
        BenefitClaim claim = claimRepo.findByClaimCodeWithLock(claimCode)
                .orElseThrow(() -> new RecordNotFoundException("Claim not found: " + claimCode));
        if (claim.getStatus() != ClaimStatus.ACTIVE)
            throw new InvalidOperationException("Claim is not ACTIVE: " + claimCode);
        claim.setStatus(ClaimStatus.SUSPENDED);
        return ClaimResponse.from(claimRepo.save(claim));
    }

    @Transactional
    public ClaimResponse reinstate(String claimCode) {
        BenefitClaim claim = claimRepo.findByClaimCodeWithLock(claimCode)
                .orElseThrow(() -> new RecordNotFoundException("Claim not found: " + claimCode));
        if (claim.getStatus() != ClaimStatus.SUSPENDED)
            throw new InvalidOperationException("Claim is not SUSPENDED: " + claimCode);
        claim.setStatus(ClaimStatus.ACTIVE);
        bridgePublisher.publishBenefitStatus(claim.getCitizenNationalId());
        return ClaimResponse.from(claimRepo.save(claim));
    }

    @Transactional
    public ClaimResponse autoTrigger(AutoTriggerRequest req) {
        citizenClient.verify(req.citizenNationalId());

        BenefitProgram program;
        if (req.programCode() != null) {
            program = programRepo.findByProgramCode(req.programCode())
                    .orElseThrow(() -> new RecordNotFoundException("Program not found: " + req.programCode()));
        } else {
            program = programRepo.findFirstByBenefitTypeAndStatus(req.benefitType(), ProgramStatus.ACTIVE)
                    .orElseThrow(() -> new RecordNotFoundException("No active program for type: " + req.benefitType()));
        }

        BenefitClaim claim = BenefitClaim.builder()
                .claimCode(generateClaimCode())
                .citizenNationalId(req.citizenNationalId())
                .programCode(program.getProgramCode())
                .claimType(ClaimType.AUTO_TRIGGERED)
                .status(ClaimStatus.ACTIVE)
                .approvedAt(LocalDateTime.now())
                .triggerReason(req.triggerReason())
                .build();

        if (program.getMaxDurationMonths() != null)
            claim.setExpiresAt(LocalDate.now().plusMonths(program.getMaxDurationMonths()));

        claimRepo.save(claim);
        paymentService.schedulePayment(claim, program);
        bridgePublisher.publishBenefitStatus(req.citizenNationalId());

        notificationClient.send(req.citizenNationalId(), "BENEFIT_AUTO_TRIGGERED", "TK",
                Map.of("claimCode", claim.getClaimCode(), "programCode", program.getProgramCode(),
                       "triggerReason", req.triggerReason() != null ? req.triggerReason() : ""));
        return ClaimResponse.from(claim);
    }

    @Transactional
    public int cancelAll(String nationalId) {
        int count = claimRepo.cancelAllForCitizen(nationalId);
        if (count > 0) bridgePublisher.publishBenefitStatus(nationalId);
        return count;
    }

    public ClaimResponse findByCode(String claimCode) {
        return claimRepo.findByClaimCode(claimCode)
                .map(ClaimResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Claim not found: " + claimCode));
    }

    public List<ClaimResponse> findByCitizen(String nationalId) {
        return claimRepo.findByCitizenNationalId(nationalId).stream().map(ClaimResponse::from).toList();
    }

    public List<ClaimResponse> findPending() {
        return claimRepo.findByStatus(ClaimStatus.PENDING).stream().map(ClaimResponse::from).toList();
    }

    protected String generateClaimCode() {
        int year = LocalDate.now().getYear();
        seqRepo.insertIfNotExists(year);
        ClaimSequence seq = seqRepo.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Sequence not initialized for year " + year));
        int next = seq.getNextValue();
        seq.setNextValue(next + 1);
        seqRepo.save(seq);
        return "TM-CLM-%d%06d".formatted(year, next);
    }

    private UUID getOfficerId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof com.sanly.social.config.UserDetailsImpl ud)
                return ud.getOfficerId();
        } catch (Exception ignored) {}
        return null;
    }
}
