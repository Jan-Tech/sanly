package com.sanly.analytics.service;

import com.sanly.analytics.entity.ExportCodeSequence;
import com.sanly.analytics.repository.ExportCodeSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExportCodeService {

    private final ExportCodeSequenceRepository seqRepo;

    @Transactional
    public String generateCode() {
        int year = LocalDate.now().getYear();
        ExportCodeSequence seq = seqRepo.findByYearWithLock(year)
                .orElseGet(() -> seqRepo.save(
                        ExportCodeSequence.builder()
                                .id(UUID.randomUUID())
                                .year(year)
                                .nextVal(1L)
                                .build()));
        long n = seq.getNextVal();
        seq.setNextVal(n + 1);
        seqRepo.save(seq);
        return String.format("TM-RPT-%d%06d", year, n);
    }
}
