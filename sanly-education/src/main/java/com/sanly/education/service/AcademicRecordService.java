package com.sanly.education.service;

import com.sanly.education.config.UserDetailsImpl;
import com.sanly.education.dto.request.AddAcademicRecordRequest;
import com.sanly.education.dto.response.AcademicRecordResponse;
import com.sanly.education.entity.AcademicRecord;
import com.sanly.education.exception.InvalidOperationException;
import com.sanly.education.repository.AcademicRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicRecordService {
    private final AcademicRecordRepository recordRepository;

    @Transactional
    public AcademicRecordResponse addRecord(AddAcademicRecordRequest req, Authentication auth) {
        if (auth != null) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            if (user.getInstitutionCode() != null &&
                    !user.getInstitutionCode().equals(req.institutionCode())) {
                throw new InvalidOperationException("You can only add records for your own institution");
            }
        }

        AcademicRecord record = AcademicRecord.builder()
                .citizenNationalId(req.citizenNationalId())
                .institutionCode(req.institutionCode())
                .academicYear(req.academicYear())
                .grade(req.grade())
                .gpa(req.gpa())
                .notes(req.notes())
                .recordedByOfficerId(auth != null
                        ? ((UserDetailsImpl) auth.getPrincipal()).getOfficerId() : null)
                .build();

        return AcademicRecordResponse.from(recordRepository.save(record));
    }

    public List<AcademicRecordResponse> findByCitizen(String nationalId) {
        return recordRepository.findByCitizenNationalId(nationalId)
                .stream().map(AcademicRecordResponse::from).toList();
    }
}
