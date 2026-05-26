package com.sanly.education.service;

import com.sanly.education.client.BridgePublisherService;
import com.sanly.education.client.NotificationClient;
import com.sanly.education.config.UserDetailsImpl;
import com.sanly.education.dto.request.IssueDiplomaRequest;
import com.sanly.education.dto.response.DiplomaResponse;
import com.sanly.education.entity.*;
import com.sanly.education.exception.InvalidOperationException;
import com.sanly.education.exception.RecordNotFoundException;
import com.sanly.education.repository.DiplomaRepository;
import com.sanly.education.repository.DiplomaSequenceRepository;
import com.sanly.education.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiplomaService {
    private final DiplomaRepository diplomaRepository;
    private final DiplomaSequenceRepository sequenceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final InstitutionService institutionService;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;

    @Transactional
    public DiplomaResponse issue(IssueDiplomaRequest req, Authentication auth) {
        var institution = institutionService.getOrThrow(req.institutionCode());

        if (auth != null) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            if (user.getInstitutionCode() != null &&
                    !user.getInstitutionCode().equals(req.institutionCode())) {
                throw new InvalidOperationException("You can only issue diplomas for your own institution");
            }
        }

        // Diploma requires a GRADUATED enrollment
        boolean hasGraduated = enrollmentRepository
                .existsByCitizenNationalIdAndInstitutionCodeAndStatus(
                        req.citizenNationalId(), req.institutionCode(), EnrollmentStatus.GRADUATED);
        if (!hasGraduated) {
            throw new InvalidOperationException(
                    "Citizen must have a GRADUATED enrollment before a diploma can be issued");
        }

        String diplomaCode = generateDiplomaCode(req.graduationDate().getYear());

        Diploma diploma = Diploma.builder()
                .diplomaCode(diplomaCode)
                .citizenNationalId(req.citizenNationalId())
                .institutionCode(req.institutionCode())
                .programName(req.programName())
                .programLevel(req.programLevel())
                .graduationDate(req.graduationDate())
                .honors(req.honors() != null ? req.honors() : Honors.NONE)
                .issuedByOfficerId(auth != null
                        ? ((UserDetailsImpl) auth.getPrincipal()).getOfficerId() : null)
                .status(DiplomaStatus.VALID)
                .bridgePublished(false)
                .build();

        Diploma saved = diplomaRepository.save(diploma);
        bridgePublisherService.publishDiploma(saved.getDiplomaId(), institution.getName());

        notificationClient.send(req.citizenNationalId(), "DIPLOMA_ISSUED", "EN",
                Map.of(
                        "diplomaCode",     diplomaCode,
                        "institutionName", institution.getName(),
                        "programName",     req.programName(),
                        "graduationDate",  req.graduationDate().toString()
                ));

        return DiplomaResponse.from(saved);
    }

    public DiplomaResponse findByCode(String code) {
        return DiplomaResponse.from(diplomaRepository.findByDiplomaCode(code)
                .orElseThrow(() -> new RecordNotFoundException("Diploma not found: " + code)));
    }

    public List<DiplomaResponse> findByCitizen(String nationalId) {
        return diplomaRepository.findByCitizenNationalId(nationalId)
                .stream().map(DiplomaResponse::from).toList();
    }

    public DiplomaResponse verifyPublic(String code) {
        return DiplomaResponse.from(diplomaRepository.findByDiplomaCode(code)
                .orElseThrow(() -> new RecordNotFoundException("Diploma not found: " + code)));
    }

    @Transactional
    public DiplomaResponse revoke(String code, String reason) {
        Diploma diploma = diplomaRepository.findByDiplomaCode(code)
                .orElseThrow(() -> new RecordNotFoundException("Diploma not found: " + code));
        if (diploma.getStatus() == DiplomaStatus.REVOKED) {
            throw new InvalidOperationException("Diploma is already revoked");
        }

        String institutionName = institutionService.getOrThrow(diploma.getInstitutionCode()).getName();

        diploma.setStatus(DiplomaStatus.REVOKED);
        diploma.setRevokedReason(reason);
        diploma.setBridgePublished(false);
        Diploma saved = diplomaRepository.save(diploma);

        bridgePublisherService.publishDiploma(saved.getDiplomaId(), institutionName);

        notificationClient.send(diploma.getCitizenNationalId(), "DIPLOMA_REVOKED", "EN",
                Map.of(
                        "diplomaCode",     code,
                        "institutionName", institutionName,
                        "reason",          reason != null ? reason : "Not specified"
                ));

        return DiplomaResponse.from(saved);
    }

    public List<DiplomaResponse> findAll() {
        return diplomaRepository.findAll().stream().map(DiplomaResponse::from).toList();
    }

    @Transactional
    protected String generateDiplomaCode(int year) {
        sequenceRepository.insertIfNotExists(year);
        DiplomaSequence seq = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Diploma sequence not found"));
        long next = seq.getNextValue();
        seq.setNextValue(next + 1);
        sequenceRepository.save(seq);
        return String.format("TM-DIP-%d%06d", year, next);
    }
}
