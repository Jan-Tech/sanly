package com.sanly.business.service;

import com.sanly.business.dto.BusinessResponse;
import com.sanly.business.entity.Business;
import com.sanly.business.entity.BusinessStatus;
import com.sanly.business.exception.BusinessNotFoundException;
import com.sanly.business.exception.InvalidOperationException;
import com.sanly.business.repository.BusinessRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BusinessServiceImpl {
    private final BusinessRepository businessRepository;

    public BusinessServiceImpl(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public Page<BusinessResponse> findAll(Pageable pageable) {
        return businessRepository.findAll(pageable).map(BusinessResponse::from);
    }

    public BusinessResponse findById(UUID id) {
        return BusinessResponse.from(businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found: " + id)));
    }

    public BusinessResponse findByRegistrationNumber(String registrationNumber) {
        return BusinessResponse.from(businessRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found: " + registrationNumber)));
    }

    public Page<BusinessResponse> findByStatus(BusinessStatus status, Pageable pageable) {
        return businessRepository.findByStatus(status, pageable).map(BusinessResponse::from);
    }

    @Transactional
    public BusinessResponse suspend(UUID id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found: " + id));
        if (business.getStatus() == BusinessStatus.REVOKED) {
            throw new InvalidOperationException("Cannot suspend a revoked business");
        }
        business.setStatus(BusinessStatus.SUSPENDED);
        return BusinessResponse.from(businessRepository.save(business));
    }

    @Transactional
    public BusinessResponse activate(UUID id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found: " + id));
        if (business.getStatus() == BusinessStatus.REVOKED) {
            throw new InvalidOperationException("Cannot activate a revoked business");
        }
        business.setStatus(BusinessStatus.ACTIVE);
        return BusinessResponse.from(businessRepository.save(business));
    }

    @Transactional
    public BusinessResponse revoke(UUID id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found: " + id));
        business.setStatus(BusinessStatus.REVOKED);
        return BusinessResponse.from(businessRepository.save(business));
    }
}
