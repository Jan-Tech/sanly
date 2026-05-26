package com.sanly.registry.service;

import com.sanly.registry.entity.DdrSequence;
import com.sanly.registry.repository.DdrSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DdrCodeService {

    private final DdrSequenceRepository ddrSequenceRepository;

    @Transactional
    public String generateCode() {
        int year = LocalDate.now().getYear();

        ddrSequenceRepository.insertIfNotExists(year);

        DdrSequence sequence = ddrSequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException(
                        "DdrSequence row for year " + year + " could not be found after insert"));

        int next = sequence.getLastSequence() + 1;
        sequence.setLastSequence(next);
        ddrSequenceRepository.save(sequence);

        return "TM-DDR-" + year + String.format("%06d", next);
    }
}
