package com.sanly.registry.service;

import AddressRequest;
import com.sanly.registry.dto.request.CitizenCreateRequest;
import com.sanly.registry.dto.request.CitizenUpdateRequest;
import com.sanly.registry.dto.request.StatusUpdateRequest;
import com.sanly.registry.dto.response.AddressResponse;
import com.sanly.registry.dto.response.CitizenResponse;
import com.sanly.registry.dto.response.CitizenVerifyResponse;
import com.sanly.registry.dto.response.PageResponse;
import com.sanly.registry.entity.Address;
import com.sanly.registry.entity.Citizen;
import com.sanly.registry.entity.CitizenStatus;
import com.sanly.registry.entity.NinSequence;
import com.sanly.registry.exception.CitizenNotFoundException;
import com.sanly.registry.exception.InvalidNationalIdException;
import com.sanly.registry.exception.RegistrationLimitExceededException;
import com.sanly.registry.exception.RelatedCitizenNotFoundException;
import com.sanly.registry.repository.CitizenRepository;
import com.sanly.registry.repository.NinSequenceRepository;
import com.sanly.registry.util.NationalIdGenerator;
import com.sanly.registry.util.NationalIdValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {

    private static final int MAX_SEQUENCE = 999;

    private final CitizenRepository     citizenRepository;
    private final NinSequenceRepository ninSequenceRepository;

    // ===== Registration =====

    @Override
    @Transactional
    public CitizenResponse register(CitizenCreateRequest req) {
        validateRelatedCitizens(req);

        String nationalId = generateNextNin(req);

        Citizen citizen = Citizen.builder()
                .nationalId(nationalId)
                .firstName(req.getFirstName().trim())
                .lastName(req.getLastName().trim())
                .middleName(req.getMiddleName() != null ? req.getMiddleName().trim() : null)
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender())
                .placeOfBirth(req.getPlaceOfBirth())
                .address(toAddressEntity(req.getAddress() != null
                        ? req.getAddress() : new AddressRequest()))
                .photoUrl(req.getPhotoUrl())
                .fatherId(req.getFatherId())
                .motherId(req.getMotherId())
                .spouseId(req.getSpouseId())
                .phoneNumber(req.getPhoneNumber())
                .build();

        Citizen saved = citizenRepository.save(citizen);
        log.info("Registered citizen NIN={}", nationalId);
        return toResponse(saved);
    }

    // ===== Read =====

    @Override
    @Transactional(readOnly = true)
    public CitizenResponse getByNationalId(String nationalId) {
        validateNin(nationalId);
        Citizen citizen = findOrThrow(nationalId);
        return toResponse(citizen);
    }

    @Override
    @Transactional(readOnly = true)
    public CitizenVerifyResponse verify(String nationalId) {
        if (!NationalIdValidator.isValid(nationalId)) {
            return CitizenVerifyResponse.builder()
                    .nationalId(nationalId)
                    .exists(false)
                    .active(false)
                    .build();
        }

        return citizenRepository.findById(nationalId)
                .map(c -> CitizenVerifyResponse.builder()
                        .nationalId(c.getNationalId())
                        .exists(true)
                        .active(c.getStatus() == CitizenStatus.ACTIVE)
                        .status(c.getStatus())
                        .fullName(buildFullName(c))
                        .build())
                .orElseGet(() -> CitizenVerifyResponse.builder()
                        .nationalId(nationalId)
                        .exists(false)
                        .active(false)
                        .build());
    }

    // ===== Update =====

    @Override
    @Transactional
    public CitizenResponse update(String nationalId, CitizenUpdateRequest req) {
        validateNin(nationalId);
        Citizen citizen = findOrThrow(nationalId);

        if (StringUtils.hasText(req.getFirstName()))   citizen.setFirstName(req.getFirstName().trim());
        if (StringUtils.hasText(req.getLastName()))    citizen.setLastName(req.getLastName().trim());
        if (req.getMiddleName() != null)               citizen.setMiddleName(req.getMiddleName().trim());
        if (StringUtils.hasText(req.getPlaceOfBirth())) citizen.setPlaceOfBirth(req.getPlaceOfBirth());
        if (req.getPhotoUrl() != null)                 citizen.setPhotoUrl(req.getPhotoUrl());
        if (req.getFatherId() != null)                 citizen.setFatherId(req.getFatherId());
        if (req.getMotherId() != null)                 citizen.setMotherId(req.getMotherId());
        if (req.getSpouseId() != null)                 citizen.setSpouseId(req.getSpouseId());

        if (req.getAddress() != null) {
            Address addr = citizen.getAddress() != null
                    ? citizen.getAddress() : new Address();
            if (req.getAddress().getStreet() != null)  addr.setStreet(req.getAddress().getStreet());
            if (req.getAddress().getCity()   != null)  addr.setCity(req.getAddress().getCity());
            if (req.getAddress().getRegion() != null)  addr.setRegion(req.getAddress().getRegion());
            citizen.setAddress(addr);
        }

        log.info("Updated citizen NIN={}", nationalId);
        return toResponse(citizenRepository.save(citizen));
    }

    @Override
    @Transactional
    public CitizenResponse updateStatus(String nationalId, StatusUpdateRequest req) {
        validateNin(nationalId);
        Citizen citizen = findOrThrow(nationalId);
        citizen.setStatus(req.getStatus());
        log.info("Status of NIN={} changed to {}", nationalId, req.getStatus());
        return toResponse(citizenRepository.save(citizen));
    }

    // ===== Search =====

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CitizenResponse> search(String name, LocalDate dob,
                                                 String region, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("lastName", "firstName"));
        Page<CitizenResponse> result = citizenRepository
                .search(
                        StringUtils.hasText(name)   ? name   : null,
                        dob,
                        StringUtils.hasText(region) ? region : null,
                        pageable)
                .map(this::toResponse);

        return PageResponse.<CitizenResponse>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    // ===== Phone update =====

    @Override
    @Transactional
    public CitizenResponse updatePhone(String nationalId, String phoneNumber) {
        validateNin(nationalId);
        Citizen citizen = findOrThrow(nationalId);
        citizen.setPhoneNumber(phoneNumber);
        log.info("Phone updated for NIN={}", nationalId);
        return toResponse(citizenRepository.save(citizen));
    }

    // ===== NIN generation (pessimistic-locked) =====

    private String generateNextNin(CitizenCreateRequest req) {
        String prefix = NationalIdGenerator.buildPrefix(req.getDateOfBirth(), req.getGender());

        // Idempotent insert — safe under concurrent registrations for same prefix
        ninSequenceRepository.insertIfNotExists(prefix);

        NinSequence seq = ninSequenceRepository.findByPrefixWithLock(prefix)
                .orElseThrow(() -> new IllegalStateException(
                        "NIN sequence row missing for prefix " + prefix));

        int next = seq.getLastSequence() + 1;
        if (next > MAX_SEQUENCE) {
            throw new RegistrationLimitExceededException(
                    "Daily registration limit reached for date "
                    + req.getDateOfBirth() + " / gender " + req.getGender());
        }

        seq.setLastSequence(next);
        ninSequenceRepository.save(seq);

        return NationalIdGenerator.generate(req.getDateOfBirth(), req.getGender(), next);
    }

    // ===== Validation helpers =====

    private void validateNin(String nationalId) {
        if (!NationalIdValidator.isValid(nationalId)) {
            throw new InvalidNationalIdException(nationalId);
        }
    }

    private Citizen findOrThrow(String nationalId) {
        return citizenRepository.findById(nationalId)
                .orElseThrow(() -> new CitizenNotFoundException(nationalId));
    }

    private void validateRelatedCitizens(CitizenCreateRequest req) {
        checkRelated("Father", req.getFatherId());
        checkRelated("Mother", req.getMotherId());
        checkRelated("Spouse", req.getSpouseId());
    }

    private void checkRelated(String relation, String nationalId) {
        if (StringUtils.hasText(nationalId)
                && !citizenRepository.existsByNationalId(nationalId)) {
            throw new RelatedCitizenNotFoundException(relation, nationalId);
        }
    }

    // ===== Mapping =====

    private CitizenResponse toResponse(Citizen c) {
        String phoneMasked = null;
        if (c.getPhoneNumber() != null && c.getPhoneNumber().length() >= 4) {
            phoneMasked = "***" + c.getPhoneNumber().substring(c.getPhoneNumber().length() - 4);
        }
        return CitizenResponse.builder()
                .nationalId(c.getNationalId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .middleName(c.getMiddleName())
                .dateOfBirth(c.getDateOfBirth())
                .gender(c.getGender())
                .placeOfBirth(c.getPlaceOfBirth())
                .address(toAddressResponse(c.getAddress()))
                .photoUrl(c.getPhotoUrl())
                .fatherId(c.getFatherId())
                .motherId(c.getMotherId())
                .spouseId(c.getSpouseId())
                .status(c.getStatus())
                .phoneMasked(phoneMasked)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private AddressResponse toAddressResponse(Address a) {
        if (a == null) return null;
        return AddressResponse.builder()
                .street(a.getStreet())
                .city(a.getCity())
                .region(a.getRegion())
                .build();
    }

    private Address toAddressEntity(AddressRequest r) {
        if (r == null) return null;
        return Address.builder()
                .street(r.getStreet())
                .city(r.getCity())
                .region(r.getRegion())
                .build();
    }

    private String buildFullName(Citizen c) {
        StringBuilder sb = new StringBuilder(c.getLastName()).append(" ").append(c.getFirstName());
        if (StringUtils.hasText(c.getMiddleName())) sb.append(" ").append(c.getMiddleName());
        return sb.toString();
    }
}
