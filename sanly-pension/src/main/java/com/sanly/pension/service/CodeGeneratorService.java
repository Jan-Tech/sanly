package com.sanly.pension.service;

import com.sanly.pension.entity.AccountSequence;
import com.sanly.pension.entity.EmployerSequence;
import com.sanly.pension.repository.AccountSequenceRepository;
import com.sanly.pension.repository.EmployerSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CodeGeneratorService {
    private final EmployerSequenceRepository employerSeqRepo;
    private final AccountSequenceRepository accountSeqRepo;

    @Transactional
    public String nextEmployerCode() {
        EmployerSequence seq = employerSeqRepo.findForUpdate()
                .orElseGet(() -> EmployerSequence.builder().id(1L).lastNumber(0L).build());
        long next = seq.getLastNumber() + 1;
        seq.setLastNumber(next);
        employerSeqRepo.save(seq);
        return String.format("TM-EMP-%04d", next);
    }

    @Transactional
    public String nextAccountCode() {
        AccountSequence seq = accountSeqRepo.findForUpdate()
                .orElseGet(() -> AccountSequence.builder().id(1L).lastNumber(0L).build());
        long next = seq.getLastNumber() + 1;
        seq.setLastNumber(next);
        accountSeqRepo.save(seq);
        int year = LocalDate.now().getYear();
        return String.format("TM-PEN-%d%06d", year, next);
    }
}
