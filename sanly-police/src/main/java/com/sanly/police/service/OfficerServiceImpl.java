package com.sanly.police.service;

import com.sanly.police.dto.request.OfficerCreateRequest;
import com.sanly.police.dto.request.OfficerStatusRequest;
import com.sanly.police.dto.response.OfficerResponse;
import com.sanly.police.entity.PoliceOfficer;
import com.sanly.police.exception.OfficerNotFoundException;
import com.sanly.police.repository.OfficerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficerServiceImpl {

    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OfficerResponse create(OfficerCreateRequest req) {
        PoliceOfficer officer = PoliceOfficer.builder()
                .firstName(req.getFirstName()).lastName(req.getLastName())
                .badgeNumber(req.getBadgeNumber()).rank(req.getRank())
                .stationRegion(req.getStationRegion())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of("ROLE_OFFICER"))
                .build();
        return toResponse(officerRepository.save(officer));
    }

    @Transactional(readOnly = true)
    public List<OfficerResponse> listAll() {
        return officerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OfficerResponse updateStatus(Long id, OfficerStatusRequest req) {
        PoliceOfficer o = officerRepository.findById(id).orElseThrow(() -> new OfficerNotFoundException(id));
        o.setStatus(req.getStatus());
        return toResponse(officerRepository.save(o));
    }

    private OfficerResponse toResponse(PoliceOfficer o) {
        return OfficerResponse.builder()
                .officerId(o.getOfficerId()).nationalId(o.getNationalId())
                .firstName(o.getFirstName()).lastName(o.getLastName())
                .badgeNumber(o.getBadgeNumber()).rank(o.getRank())
                .stationRegion(o.getStationRegion()).status(o.getStatus())
                .createdAt(o.getCreatedAt()).build();
    }
}
