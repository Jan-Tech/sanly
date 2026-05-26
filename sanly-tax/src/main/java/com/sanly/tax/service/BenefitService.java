package com.sanly.tax.service;

import com.sanly.tax.entity.BenefitStatus;
import com.sanly.tax.entity.ChildBenefitRecord;
import com.sanly.tax.entity.MaritalStatus;
import com.sanly.tax.entity.TaxpayerStatus;
import com.sanly.tax.repository.ChildBenefitRepository;
import com.sanly.tax.repository.TaxpayerRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitService {

    private final ChildBenefitRepository  childBenefitRepository;
    private final TaxpayerRecordRepository taxpayerRecordRepository;

    @Transactional
    public ChildBenefitRecord createChildBenefit(String childNationalId, String motherNationalId,
                                                   String fatherNationalId, LocalDate birthDate) {
        if (childBenefitRepository.existsByChildNationalId(childNationalId)) {
            log.info("Child benefit already exists for NIN={}, skipping", childNationalId);
            return childBenefitRepository.findAll().stream()
                    .filter(b -> childNationalId.equals(b.getChildNationalId()))
                    .findFirst().orElse(null);
        }

        ChildBenefitRecord record = ChildBenefitRecord.builder()
                .childNationalId(childNationalId)
                .motherNationalId(motherNationalId)
                .fatherNationalId(fatherNationalId)
                .birthDate(birthDate)
                .status(BenefitStatus.ACTIVE)
                .build();

        ChildBenefitRecord saved = childBenefitRepository.save(record);
        log.info("Child benefit created for child NIN={}", childNationalId);
        return saved;
    }

    @Transactional
    public int cancelAllBenefits(String nationalId) {
        int cancelled = childBenefitRepository.cancelAllForCitizen(
                nationalId, BenefitStatus.CANCELLED, LocalDateTime.now());
        log.info("Cancelled {} benefit records for NIN={}", cancelled, nationalId);
        return cancelled;
    }

    @Transactional
    public void deregisterTaxpayer(String nationalId) {
        taxpayerRecordRepository.findByCitizenNationalId(nationalId).ifPresent(record -> {
            record.setStatus(TaxpayerStatus.DEREGISTERED);
            taxpayerRecordRepository.save(record);
            log.info("Taxpayer NIN={} deregistered (deceased)", nationalId);
        });
    }

    @Transactional
    public void updateMaritalStatus(String nationalId, MaritalStatus status, String spouseNationalId) {
        taxpayerRecordRepository.findByCitizenNationalId(nationalId).ifPresent(record -> {
            record.setMaritalStatus(status);
            record.setSpouseNationalId(spouseNationalId);
            taxpayerRecordRepository.save(record);
            log.info("Marital status updated to {} for NIN={}", status, nationalId);
        });
    }
}
