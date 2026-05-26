package com.sanly.banking.service;

import com.sanly.banking.entity.BankingCodeSequence;
import com.sanly.banking.repository.BankingCodeSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentCodeService {

    private static final String SEQUENCE_TYPE = "CONSENT";

    private final BankingCodeSequenceRepository sequenceRepository;

    /**
     * Generates next TM-CNS-YYYYNNNNNN code.
     * Uses PESSIMISTIC_WRITE to prevent concurrent duplicates.
     */
    @Transactional
    public String generateNextConsentCode() {
        int currentYear = LocalDateTime.now().getYear();

        BankingCodeSequence seq = sequenceRepository.findBySequenceTypeWithLock(SEQUENCE_TYPE)
                .orElseThrow(() -> new IllegalStateException("CONSENT sequence not initialised in database"));

        // Reset counter if year has changed
        if (seq.getYear() != currentYear) {
            seq.setYear(currentYear);
            seq.setNextVal(1L);
        }

        long n = seq.getNextVal();
        seq.setNextVal(n + 1);
        sequenceRepository.save(seq);

        return String.format("TM-CNS-%04d%06d", currentYear, n);
    }
}
