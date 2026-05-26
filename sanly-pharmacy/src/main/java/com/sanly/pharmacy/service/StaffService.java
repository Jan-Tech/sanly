package com.sanly.pharmacy.service;

import com.sanly.pharmacy.config.UserDetailsImpl;
import com.sanly.pharmacy.dto.request.CreateStaffRequest;
import com.sanly.pharmacy.dto.request.StaffStatusRequest;
import com.sanly.pharmacy.dto.response.StaffResponse;
import com.sanly.pharmacy.entity.PharmacyStaff;
import com.sanly.pharmacy.entity.StaffStatus;
import com.sanly.pharmacy.repository.PharmacyStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final PharmacyStaffRepository staffRepository;
    private final PasswordEncoder         passwordEncoder;

    @Transactional
    public StaffResponse create(CreateStaffRequest req, UserDetailsImpl principal) {
        if (staffRepository.existsByNationalId(req.getNationalId())) {
            throw new IllegalArgumentException("NIN already registered: " + req.getNationalId());
        }
        if (staffRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + req.getUsername());
        }
        PharmacyStaff staff = PharmacyStaff.builder()
                .nationalId(req.getNationalId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .pharmacyCode(principal.getPharmacyCode())
                .role(req.getRole())
                .status(StaffStatus.ACTIVE)
                .build();
        return StaffResponse.from(staffRepository.save(staff));
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> listAll() {
        return staffRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(StaffResponse::from).toList();
    }

    @Transactional
    public StaffResponse updateStatus(Long staffId, StaffStatusRequest req) {
        PharmacyStaff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found: " + staffId));
        staff.setStatus(req.getStatus());
        return StaffResponse.from(staffRepository.save(staff));
    }
}
