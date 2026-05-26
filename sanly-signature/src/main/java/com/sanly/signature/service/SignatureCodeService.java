package com.sanly.signature.service;

import com.sanly.signature.entity.SignatureSequence;
import com.sanly.signature.repository.SignatureSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SignatureCodeService {

    private final SignatureSequenceRepository sequenceRepository;

    public SignatureCodeService(SignatureSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates the next unique TM-SIG-YYYYnnnnnn code.
     * Uses a pessimistic write lock on the sequence row to prevent duplicate codes
     * under concurrent signing requests.
     *
     * Example output: "TM-SIG-202600000001"
     */
    @Transactional
    public String generateCode() {
        int year = LocalDate.now().getYear();

        // Ensure the row exists (idempotent — ON CONFLICT DO NOTHING)
        sequenceRepository.insertIfNotExists(year);

        // Acquire pessimistic lock on the row
        SignatureSequence seq = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException(
                        "Failed to acquire sequence lock for year " + year));

        int next = seq.getLastSequence() + 1;
        seq.setLastSequence(next);
        sequenceRepository.save(seq);

        return "TM-SIG-" + year + String.format("%06d", next);
    }
}
