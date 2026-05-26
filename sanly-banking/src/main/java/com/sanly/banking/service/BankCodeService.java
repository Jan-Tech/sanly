package com.sanly.banking.service;

import com.sanly.banking.entity.BankingCodeSequence;
import com.sanly.banking.repository.BankingCodeSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankCodeService {

    private static final String SEQUENCE_TYPE = "BANK";

    private final BankingCodeSequenceRepository sequenceRepository;

    /**
     * Generates next TM-BNK-NNN code.
     * Uses PESSIMISTIC_WRITE to prevent concurrent duplicates.
     */
    @Transactional
    public String generateNextBankCode() {
        BankingCodeSequence seq = sequenceRepository.findBySequenceTypeWithLock(SEQUENCE_TYPE)
                .orElseThrow(() -> new IllegalStateException("BANK sequence not initialised in database"));

        long n = seq.getNextVal();
        seq.setNextVal(n + 1);
        sequenceRepository.save(seq);

        return "TM-BNK-" + String.format("%03d", n);
    }
}
