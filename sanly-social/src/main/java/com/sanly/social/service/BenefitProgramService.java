package com.sanly.social.service;

import com.sanly.social.dto.request.CreateProgramRequest;
import com.sanly.social.dto.response.ProgramResponse;
import com.sanly.social.entity.BenefitProgram;
import com.sanly.social.entity.ProgramStatus;
import com.sanly.social.exception.RecordNotFoundException;
import com.sanly.social.repository.BenefitProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenefitProgramService {

    private final BenefitProgramRepository programRepo;

    @Transactional
    public ProgramResponse create(CreateProgramRequest req) {
        int seq = programRepo.findMaxProgramSequence() + 1;
        String code = "TM-BEN-%04d".formatted(seq);

        BenefitProgram program = BenefitProgram.builder()
                .programCode(code)
                .name(req.name())
                .description(req.description())
                .benefitType(req.benefitType())
                .monthlyAmount(req.monthlyAmount())
                .eligibilityCriteria(req.eligibilityCriteria())
                .maxDurationMonths(req.maxDurationMonths())
                .status(ProgramStatus.ACTIVE)
                .build();
        return ProgramResponse.from(programRepo.save(program));
    }

    public List<ProgramResponse> findAll() {
        return programRepo.findAll().stream().map(ProgramResponse::from).toList();
    }

    public ProgramResponse findByCode(String programCode) {
        return programRepo.findByProgramCode(programCode)
                .map(ProgramResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Program not found: " + programCode));
    }

    @Transactional
    public ProgramResponse updateStatus(String programCode, ProgramStatus status) {
        BenefitProgram program = programRepo.findByProgramCode(programCode)
                .orElseThrow(() -> new RecordNotFoundException("Program not found: " + programCode));
        program.setStatus(status);
        return ProgramResponse.from(programRepo.save(program));
    }
}
