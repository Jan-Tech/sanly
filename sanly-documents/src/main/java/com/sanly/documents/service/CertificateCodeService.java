package com.sanly.documents.service;

import com.sanly.documents.entity.CertificateSequence;
import com.sanly.documents.repository.CertificateSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CertificateCodeService {

    private final CertificateSequenceRepository sequenceRepository;

    public CertificateCodeService(CertificateSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a unique certificate code in format TM-CERT-YYYYNNNNNN.
     * Uses PESSIMISTIC_WRITE lock to ensure uniqueness under concurrent load.
     */
    @Transactional
    public String generateCode() {
        int year = LocalDate.now().getYear();

        // Ensure the row exists for this year
        sequenceRepository.insertIfNotExists(year);

        // Lock and increment
        CertificateSequence seq = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Certificate sequence row missing for year " + year));

        int next = seq.getLastSequence() + 1;
        seq.setLastSequence(next);
        sequenceRepository.save(seq);

        // TM-CERT-YYYYNNNNNN (10-digit zero-padded sequence)
        return String.format("TM-CERT-%d%06d", year, next);
    }
}
