package com.sanly.court.service;

import com.sanly.court.client.NotificationClient;
import com.sanly.court.dto.request.CitizenPayRequest;
import com.sanly.court.dto.request.IssueFineRequest;
import com.sanly.court.dto.response.FineResponse;
import com.sanly.court.entity.CourtFine;
import com.sanly.court.entity.FineSequence;
import com.sanly.court.entity.FineStatus;
import com.sanly.court.exception.InvalidOperationException;
import com.sanly.court.exception.RecordNotFoundException;
import com.sanly.court.repository.CourtCaseRepository;
import com.sanly.court.repository.CourtFineRepository;
import com.sanly.court.repository.FineSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FineService {

    private final CourtFineRepository fineRepo;
    private final FineSequenceRepository seqRepo;
    private final CourtCaseRepository caseRepo;
    private final NotificationClient notificationClient;

    @Transactional
    public FineResponse issue(IssueFineRequest req) {
        caseRepo.findByCaseNumber(req.caseNumber())
                .orElseThrow(() -> new RecordNotFoundException("Case not found: " + req.caseNumber()));

        CourtFine fine = CourtFine.builder()
                .fineCode(generateFineCode())
                .caseNumber(req.caseNumber())
                .citizenNationalId(req.citizenNationalId())
                .amount(req.amount())
                .reason(req.reason())
                .dueDate(req.dueDate())
                .notes(req.notes())
                .status(FineStatus.OUTSTANDING)
                .build();
        fineRepo.save(fine);

        notificationClient.send(req.citizenNationalId(), "COURT_FINE_ISSUED", "TK",
                Map.of("fineCode", fine.getFineCode(), "caseNumber", req.caseNumber(),
                       "amount", req.amount(), "dueDate", req.dueDate().toString()));
        return FineResponse.from(fine);
    }

    public FineResponse findByCode(String fineCode) {
        return fineRepo.findByFineCode(fineCode)
                .map(FineResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Fine not found: " + fineCode));
    }

    public List<FineResponse> findByCitizen(String nationalId) {
        return fineRepo.findByCitizenNationalId(nationalId).stream().map(FineResponse::from).toList();
    }

    // Public verify
    public FineResponse verify(String fineCode) {
        return findByCode(fineCode);
    }

    @Transactional
    public FineResponse markPaid(String fineCode) {
        CourtFine fine = fineRepo.findByFineCode(fineCode)
                .orElseThrow(() -> new RecordNotFoundException("Fine not found: " + fineCode));
        if (fine.getStatus() == FineStatus.PAID)
            throw new InvalidOperationException("Fine is already PAID: " + fineCode);
        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now());
        fineRepo.save(fine);
        notificationClient.send(fine.getCitizenNationalId(), "FINE_PAYMENT_CONFIRMED", "TK",
                Map.of("fineCode", fineCode, "amount", fine.getAmount()));
        return FineResponse.from(fine);
    }

    @Transactional
    public FineResponse citizenPay(String fineCode, CitizenPayRequest req) {
        CourtFine fine = fineRepo.findByFineCode(fineCode)
                .orElseThrow(() -> new RecordNotFoundException("Fine not found: " + fineCode));

        if (!fine.getCitizenNationalId().equals(req.citizenNationalId()))
            throw new InvalidOperationException("Fine does not belong to citizen: " + req.citizenNationalId());

        if (fine.getStatus() == FineStatus.PAID)
            throw new InvalidOperationException("Fine is already PAID: " + fineCode);

        fine.setStatus(FineStatus.PENDING_VERIFICATION);
        fine.setPaymentProofNote(req.paymentProofNote());
        fineRepo.save(fine);

        notificationClient.send(fine.getCitizenNationalId(), "FINE_PAYMENT_PENDING_VERIFICATION", "TK",
                Map.of("fineCode", fineCode, "amount", fine.getAmount()));
        return FineResponse.from(fine);
    }

    @Transactional
    public FineResponse verifyPayment(String fineCode) {
        CourtFine fine = fineRepo.findByFineCode(fineCode)
                .orElseThrow(() -> new RecordNotFoundException("Fine not found: " + fineCode));
        if (fine.getStatus() != FineStatus.PENDING_VERIFICATION)
            throw new InvalidOperationException("Fine is not PENDING_VERIFICATION: " + fineCode);
        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now());
        fineRepo.save(fine);
        notificationClient.send(fine.getCitizenNationalId(), "FINE_PAYMENT_CONFIRMED", "TK",
                Map.of("fineCode", fineCode, "amount", fine.getAmount()));
        return FineResponse.from(fine);
    }

    @Transactional
    public FineResponse waive(String fineCode, String reason) {
        CourtFine fine = fineRepo.findByFineCode(fineCode)
                .orElseThrow(() -> new RecordNotFoundException("Fine not found: " + fineCode));
        if (fine.getStatus() == FineStatus.PAID)
            throw new InvalidOperationException("Cannot waive a PAID fine");
        fine.setStatus(FineStatus.WAIVED);
        fine.setNotes(reason);
        return FineResponse.from(fineRepo.save(fine));
    }

    protected String generateFineCode() {
        int year = LocalDate.now().getYear();
        seqRepo.insertIfNotExists(year);
        FineSequence seq = seqRepo.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Sequence missing for year " + year));
        int next = seq.getNextValue();
        seq.setNextValue(next + 1);
        seqRepo.save(seq);
        return "TM-FINE-%d%06d".formatted(year, next);
    }
}
