package com.sanly.appointments.service;

import com.sanly.appointments.dto.request.CreateOfficeRequest;
import com.sanly.appointments.dto.response.OfficeResponse;
import com.sanly.appointments.entity.GovernmentOffice;
import com.sanly.appointments.entity.InstitutionType;
import com.sanly.appointments.entity.OfficeStatus;
import com.sanly.appointments.repository.GovernmentOfficeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficeService {

    private final GovernmentOfficeRepository officeRepo;

    @Transactional
    public OfficeResponse createOffice(CreateOfficeRequest req) {
        GovernmentOffice office = GovernmentOffice.builder()
                .officeCode(req.getOfficeCode())
                .institutionType(req.getInstitutionType())
                .name(req.getName())
                .region(req.getRegion())
                .address(req.getAddress())
                .phone(req.getPhone())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .status(OfficeStatus.ACTIVE)
                .build();
        return toResponse(officeRepo.save(office));
    }

    @Transactional(readOnly = true)
    public List<OfficeResponse> listOffices(InstitutionType institutionType, String region) {
        List<GovernmentOffice> offices;
        if (institutionType != null && region != null) {
            offices = officeRepo.findByInstitutionTypeAndRegionAndStatusOrderByNameAsc(
                    institutionType, region, OfficeStatus.ACTIVE);
        } else if (institutionType != null) {
            offices = officeRepo.findByInstitutionTypeAndStatusOrderByNameAsc(institutionType, OfficeStatus.ACTIVE);
        } else if (region != null) {
            offices = officeRepo.findByRegionAndStatusOrderByNameAsc(region, OfficeStatus.ACTIVE);
        } else {
            offices = officeRepo.findByStatusOrderByNameAsc(OfficeStatus.ACTIVE);
        }
        return offices.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OfficeResponse getByCode(String officeCode) {
        return toResponse(officeRepo.findByOfficeCode(officeCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Office not found: " + officeCode)));
    }

    @Transactional
    public OfficeResponse updateStatus(String officeCode, OfficeStatus status) {
        GovernmentOffice office = officeRepo.findByOfficeCode(officeCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Office not found."));
        office.setStatus(status);
        log.info("Office {} status → {}", officeCode, status);
        return toResponse(officeRepo.save(office));
    }

    private OfficeResponse toResponse(GovernmentOffice o) {
        return OfficeResponse.builder()
                .officeId(o.getOfficeId()).officeCode(o.getOfficeCode())
                .institutionType(o.getInstitutionType()).name(o.getName())
                .region(o.getRegion()).address(o.getAddress()).phone(o.getPhone())
                .latitude(o.getLatitude()).longitude(o.getLongitude())
                .status(o.getStatus()).createdAt(o.getCreatedAt())
                .build();
    }
}
