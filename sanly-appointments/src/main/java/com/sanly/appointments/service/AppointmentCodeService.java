package com.sanly.appointments.service;

import com.sanly.appointments.entity.AppointmentSequence;
import com.sanly.appointments.repository.AppointmentSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentCodeService {

    private final AppointmentSequenceRepository sequenceRepository;

    /**
     * Generates a unique appointment code using PESSIMISTIC_WRITE sequence.
     * Format: TM-APT-{year}{%06d}
     * Example: TM-APT-2026000001
     */
    @Transactional
    public String generateCode() {
        int year = LocalDate.now().getYear();

        // Ensure row exists for this year
        sequenceRepository.insertIfNotExists(year);

        // Acquire pessimistic write lock and increment
        AppointmentSequence sequence = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Could not acquire appointment sequence for year " + year));

        int nextSeq = sequence.getLastSequence() + 1;
        sequence.setLastSequence(nextSeq);
        sequenceRepository.save(sequence);

        return String.format("TM-APT-%d%06d", year, nextSeq);
    }
}
