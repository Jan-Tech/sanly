package com.sanly.vehicle.service;

import com.sanly.vehicle.entity.PlateSequence;
import com.sanly.vehicle.repository.PlateSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlateNumberService {
    private final PlateSequenceRepository sequenceRepository;

    /**
     * Generates next plate number in format TM-AA-0001, TM-AA-0002, ..., TM-AA-9999, TM-AB-0001, ...
     * Uses PESSIMISTIC_WRITE lock to prevent duplicates under concurrent registrations.
     */
    @Transactional
    public String nextPlateNumber() {
        PlateSequence seq = sequenceRepository.findForUpdate()
                .orElseGet(() -> PlateSequence.builder().id(1L).lastNumber(0L).build());
        long next = seq.getLastNumber() + 1;
        seq.setLastNumber(next);
        sequenceRepository.save(seq);
        return format(next);
    }

    private String format(long n) {
        long pairIndex = (n - 1) / 9999;
        long num = ((n - 1) % 9999) + 1;
        char letter1 = (char) ('A' + pairIndex / 26);
        char letter2 = (char) ('A' + pairIndex % 26);
        return String.format("TM-%c%c-%04d", letter1, letter2, num);
    }
}
