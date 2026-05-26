package com.sanly.business.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanly.business.client.BridgePublisherService;
import com.sanly.business.client.BridgeQueryService;
import com.sanly.business.client.CitizenRegistryClient;
import com.sanly.business.client.NotificationClient;
import com.sanly.business.dto.ApplicationResponse;
import com.sanly.business.dto.CreateApplicationRequest;
import com.sanly.business.dto.ProcessApplicationRequest;
import com.sanly.business.entity.*;
import com.sanly.business.exception.ApplicationNotFoundException;
import com.sanly.business.exception.InvalidOperationException;
import com.sanly.business.repository.BusinessRepository;
import com.sanly.business.repository.BusinessSequenceRepository;
import com.sanly.business.repository.RegistrationApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicationServiceImpl {
    private final RegistrationApplicationRepository applicationRepository;
    private final BusinessRepository businessRepository;
    private final BusinessSequenceRepository sequenceRepository;
    private final CitizenRegistryClient citizenRegistryClient;
    private final BridgeQueryService bridgeQueryService;
    private final BridgePublisherService bridgePublisherService;
    private final NotificationClient notificationClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApplicationServiceImpl(RegistrationApplicationRepository applicationRepository,
                                   BusinessRepository businessRepository,
                                   BusinessSequenceRepository sequenceRepository,
                                   CitizenRegistryClient citizenRegistryClient,
                                   BridgeQueryService bridgeQueryService,
                                   BridgePublisherService bridgePublisherService,
                                   NotificationClient notificationClient) {
        this.applicationRepository = applicationRepository;
        this.businessRepository = businessRepository;
        this.sequenceRepository = sequenceRepository;
        this.citizenRegistryClient = citizenRegistryClient;
        this.bridgeQueryService = bridgeQueryService;
        this.bridgePublisherService = bridgePublisherService;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public ApplicationResponse submit(CreateApplicationRequest req) {
        citizenRegistryClient.verify(req.applicantNationalId());

        List<Map<String, Object>> taxData = bridgeQueryService.query(req.applicantNationalId(), "TAX_STATUS");
        List<Map<String, Object>> criminalData = bridgeQueryService.query(req.applicantNationalId(), "CRIMINAL_RECORD");

        RegistrationApplication app = new RegistrationApplication();
        app.setApplicantNationalId(req.applicantNationalId());
        app.setApplicantFullName(req.applicantFullName());
        app.setProposedBusinessName(req.proposedBusinessName());
        app.setBusinessType(req.businessType());
        app.setProposedAddress(req.proposedAddress());
        app.setStatus(ApplicationStatus.PENDING);
        app.setTaxCheckResult(toJson(taxData));
        app.setCriminalCheckResult(toJson(criminalData));

        return ApplicationResponse.from(applicationRepository.save(app));
    }

    @Transactional
    public ApplicationResponse process(UUID id, ProcessApplicationRequest req) {
        RegistrationApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + id));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidOperationException("Application already processed");
        }

        if (req.approve()) {
            Business business = createBusiness(app);
            app.setStatus(ApplicationStatus.APPROVED);
            app.setApprovedBusinessId(business.getId());
            applicationRepository.save(app);
            bridgePublisherService.publishBusinessRegistration(business.getId());

            notificationClient.send(app.getApplicantNationalId(), "BUSINESS_REGISTRATION_APPROVED", "EN",
                    Map.of(
                            "businessName",        app.getProposedBusinessName(),
                            "registrationNumber",  business.getRegistrationNumber(),
                            "approvedAt",          business.getRegistrationDate().toString()
                    ));
        } else {
            if (req.rejectionReason() == null || req.rejectionReason().isBlank()) {
                throw new InvalidOperationException("Rejection reason is required");
            }
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason(req.rejectionReason());
            applicationRepository.save(app);

            notificationClient.send(app.getApplicantNationalId(), "BUSINESS_REGISTRATION_REJECTED", "EN",
                    Map.of(
                            "businessName",    app.getProposedBusinessName(),
                            "rejectionReason", req.rejectionReason()
                    ));
        }

        return ApplicationResponse.from(app);
    }

    private Business createBusiness(RegistrationApplication app) {
        int year = LocalDate.now().getYear();
        String regNumber = generateRegistrationNumber(year);

        Business business = new Business();
        business.setRegistrationNumber(regNumber);
        business.setBusinessName(app.getProposedBusinessName());
        business.setBusinessType(app.getBusinessType());
        business.setOwnerNationalId(app.getApplicantNationalId());
        business.setOwnerFullName(app.getApplicantFullName());
        business.setAddress(app.getProposedAddress());
        business.setStatus(BusinessStatus.ACTIVE);
        business.setRegistrationDate(LocalDate.now());
        business.setExpiryDate(LocalDate.now().plusYears(1));
        business.setBridgePublished(false);
        return businessRepository.save(business);
    }

    @Transactional
    protected String generateRegistrationNumber(int year) {
        sequenceRepository.insertIfNotExists(year);
        BusinessSequence seq = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Sequence not found"));
        long next = seq.getLastValue() + 1;
        seq.setLastValue(next);
        sequenceRepository.save(seq);
        return String.format("TM-BUS-%d%06d", year, next);
    }

    public Page<ApplicationResponse> findAll(Pageable pageable) {
        return applicationRepository.findAll(pageable).map(ApplicationResponse::from);
    }

    public Page<ApplicationResponse> findByStatus(ApplicationStatus status, Pageable pageable) {
        return applicationRepository.findByStatus(status, pageable).map(ApplicationResponse::from);
    }

    public ApplicationResponse findById(UUID id) {
        return ApplicationResponse.from(applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + id)));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
