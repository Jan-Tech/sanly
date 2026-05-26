package com.sanly.land.service;

import com.sanly.land.dto.request.RegisterPropertyRequest;
import com.sanly.land.dto.response.OwnershipResponse;
import com.sanly.land.dto.response.PropertyDetailResponse;
import com.sanly.land.dto.response.PropertyResponse;
import com.sanly.land.entity.*;
import com.sanly.land.exception.RecordNotFoundException;
import com.sanly.land.repository.OwnershipRepository;
import com.sanly.land.repository.PropertyRepository;
import com.sanly.land.repository.PropertySequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertySequenceRepository sequenceRepository;
    private final OwnershipRepository ownershipRepository;

    @Transactional
    public PropertyResponse register(RegisterPropertyRequest req) {
        int year = LocalDate.now().getYear();
        sequenceRepository.insertIfNotExists(year);
        PropertySequence seq = sequenceRepository.findByYearWithLock(year)
                .orElseThrow(() -> new IllegalStateException("Property sequence not found"));
        long next = seq.getNextValue();
        seq.setNextValue(next + 1);
        sequenceRepository.save(seq);
        String cadastralNumber = String.format("TM-CAD-%d%06d", year, next);

        Property property = Property.builder()
                .cadastralNumber(cadastralNumber)
                .propertyType(req.propertyType())
                .address(req.address())
                .region(req.region())
                .area(req.area())
                .description(req.description())
                .status(PropertyStatus.REGISTERED)
                .build();

        return PropertyResponse.from(propertyRepository.save(property));
    }

    public PropertyDetailResponse findByCode(String cadastralNumber) {
        Property p = getOrThrow(cadastralNumber);
        List<OwnershipResponse> owners = ownershipRepository
                .findByCadastralNumberAndStatus(cadastralNumber, OwnershipStatus.ACTIVE)
                .stream().map(OwnershipResponse::from).toList();
        return new PropertyDetailResponse(PropertyResponse.from(p), owners);
    }

    public List<PropertyDetailResponse> findByOwner(String nationalId) {
        return ownershipRepository.findByOwnerNationalIdAndStatus(nationalId, OwnershipStatus.ACTIVE)
                .stream().map(o -> findByCode(o.getCadastralNumber())).toList();
    }

    public PropertyDetailResponse verifyPublic(String cadastralNumber) {
        return findByCode(cadastralNumber);
    }

    @Transactional
    public PropertyResponse updateStatus(String cadastralNumber, PropertyStatus status) {
        Property p = getOrThrow(cadastralNumber);
        p.setStatus(status);
        return PropertyResponse.from(propertyRepository.save(p));
    }

    public Property getOrThrow(String cadastralNumber) {
        return propertyRepository.findByCadastralNumber(cadastralNumber)
                .orElseThrow(() -> new RecordNotFoundException("Property not found: " + cadastralNumber));
    }
}
