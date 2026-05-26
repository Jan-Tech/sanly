package com.sanly.documents.service;

import com.sanly.documents.entity.TrackingSequence;
import com.sanly.documents.repository.TrackingSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TrackingCodeService {

    private final TrackingSequenceRepository sequenceRepository;

    public TrackingCodeService(TrackingSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a unique tracking code in format TM-TRK-YYYYNNNNNN.
     * Uses PESSIMISTIC_WRITE lock to ensure uniqueness under concurrent load.
     */
    @Transactional
    public String generateCode() {
        int year = LocalDate.now().getYear();

        // Ensure the row exists for this year
        sequenceRepository.insertIfNotExists(year);

        // Lock and increment
        TrackingSequence seq = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Tracking sequence row missing for year " + year));

        int next = seq.getLastSequence() + 1;
        seq.setLastSequence(next);
        sequenceRepository.save(seq);

        // TM-TRK-YYYYNNNNNN (10-digit zero-padded sequence)
        return String.format("TM-TRK-%d%06d", year, next);
    }
}
